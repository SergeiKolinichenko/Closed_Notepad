package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteEditBinding
import info.sergeikolinichenko.closednotepad.presentation.di.NotesApp
import info.sergeikolinichenko.closednotepad.presentation.utils.BiometricVerification
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEdit
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNotesFactory
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
Editing element of notebook
create 28.07.2022 18:21 by Sergei Kolinichenko
 **/

class NoteEditFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelNotesFactory
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteEdit::class.java]
    }

    private var _binding: FragmentNoteEditBinding? = null
    private val binding: FragmentNoteEditBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteEditBinding equals null")

    // mode device day or night
    private var isNight = false

    // mode fragment add or edit
    private var workingMode: String = MODE_UNKNOWN
    private var behaviorColorButtons = BottomSheetBehavior<ConstraintLayout>()
    private var behaviorBottomAppBar = HideBottomViewOnScrollBehavior<BottomAppBar>()

    private val component by lazy {
        (requireActivity().application as NotesApp)
            .component
            .fragmentComponentFactory()
            .create(this)
    }

    @Inject
    lateinit var biometricVerification: BiometricVerification

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
        isNightMode()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeIsLock()
        observeColorIndex()
        observeNote()
        observeRetryNoteListFrag()
        observeGetSaveOption()
        observeIsShowColorButtons()
        initEditTextTitle()     // hide edit popup menu title note EditText
        initEditTextItself()    // hide edit popup menu note itself EditText
        initLockButton()
        initBackPressed()
        initColorButtons()
        initActionBar()
        initBottomAppBar()
        initFabExit()
        initFabSave()
        initFabNoSave()
        launchModes()
    }

    private fun getNoteEntry() {
        viewModel.getNoteEntry()
    }

    private fun observeRetryNoteListFrag() {
        viewModel.retryNoteListFrag.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                hideSoftKeyboard()
            }
            retryNoteListFragment()
        }
    }

    private fun observeGetSaveOption() {
        viewModel.getSaveOption.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    showSaveFab()
                    showNoSaveFab()
                } else {
                    hideSaveFab()
                    hideNoSaveFab()
                }
            }
        }
    }

    // hide edit popup menu title note EditText
    private fun initEditTextTitle() {
        binding.etNoteEditTitle.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                //to keep the text selection capability available ( selection cursor)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                //to prevent the menu from appearing
                p1?.clear()
                return false
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: ActionMode?) {}
        }
    }

    // hide edit popup menu note itself EditText
    private fun initEditTextItself() {
        binding.etNoteEditItself.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                //to keep the text selection capability available ( selection cursor)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                //to prevent the menu from appearing
                p1?.clear()
                return false
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: ActionMode?) {}
        }
    }

    private fun initLockButton() {
        // if enabled, then turn it off, if not, then check the possibility of locking the screen
        binding.cvNoteEditLock.setOnClickListener {
            if (viewModel.isLock.value == true) {
                viewModel.changeIsLock()
            } else {
                // check the possibility of locking the screen
                testBiometricSuccess()
            }
        }
    }

    private fun initFabExit() {
        binding.fabNoteEditExit.setOnClickListener {
            exitFragment()
        }
    }

    private fun initFabSave() {
        binding.fabNoteEditSave.setOnClickListener {
            when (workingMode) {
                MODE_ADD -> viewModel.addNoteDatabase()
                MODE_EDIT -> viewModel.editNoteDatabase()
            }
        }
    }

    private fun initFabNoSave() {
        binding.fabNoteEditNotSave.setOnClickListener {
            viewModel.retryNoteListFragment()
        }
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    exitFragment()
                }
            }
        )
    }

    private fun initActionBar() {
        if (isNight) {
            binding.ivNoteEditCreate.setImageResource(R.drawable.ic_pencil_white_36dp)
        } else {
            binding.ivNoteEditCreate.setImageResource(R.drawable.ic_pencil_black_36dp)
        }
    }

    private fun initBottomAppBar() {
        initImageBottomAppBar()
        initOnClickListenerBottomAppBar()
        behaviorBottomAppBar = binding.babNoteEdit.behavior
    }

    private fun initImageBottomAppBar() {
        with(binding) {
            if (isNight) {
                ibNoteEditSelectAll.setImageResource(R.drawable.ic_select_all_white_36dp)
                ibNoteEditCut.setImageResource(R.drawable.ic_content_cut_white_36dp)
                ibNoteEditCopy.setImageResource(R.drawable.ic_content_copy_white_36dp)
                ibNoteEditPaste.setImageResource(R.drawable.ic_content_paste_white_36dp)
                ibNoteEditPallet.setImageResource(R.drawable.ic_palette_white_36dp)
            } else {
                ibNoteEditSelectAll.setImageResource(R.drawable.ic_select_all_black_36dp)
                ibNoteEditCut.setImageResource(R.drawable.ic_content_cut_black_36dp)
                ibNoteEditCopy.setImageResource(R.drawable.ic_content_copy_black_36dp)
                ibNoteEditPaste.setImageResource(R.drawable.ic_content_paste_black_36dp)
                ibNoteEditPallet.setImageResource(R.drawable.ic_palette_black_36dp)
            }
        }
    }

    private fun initOnClickListenerBottomAppBar() {
        with(binding) {
            ibNoteEditSelectAll.setOnClickListener {
                selectAll()
            }
            ibNoteEditCut.setOnClickListener {
                getCutCopyString()
            }
            ibNoteEditCopy.setOnClickListener {
                getCopyString()
            }
            ibNoteEditPaste.setOnClickListener {
                setCopyFromClip()
            }
            ibNoteEditPallet.setOnClickListener {
                getEditColorNote()
            }
        }
    }

    private fun initColorButtons() {
        behaviorColorButtons = BottomSheetBehavior.from(binding.clNoteEditColorButtons)
        behaviorColorButtons.state = BottomSheetBehavior.STATE_HIDDEN

        setColorToColorButtons()
        initOnClickListenerColorButtons()
    }

    private fun initOnClickListenerColorButtons() {
        with(binding) {
            mbNoteEditColorPink.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PINK)
            }
            mbNoteEditColorPurple.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PURPLE)
            }
            mbNoteEditColorIndigo.setOnClickListener {
                viewModel.setColorIndex(NoteColors.INDIGO)
            }
            mbNoteEditColorGreen.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GREEN)
            }
            mbNoteEditColorOrange.setOnClickListener {
                viewModel.setColorIndex(NoteColors.ORANGE)
            }
            mbNoteEditColorBrown.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BROWN)
            }
            mbNoteEditColorGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GRAY)
            }
            mbNoteEditColorBlueGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BLUE_GRAY)
            }
        }
    }

    private fun setColorToColorButtons() {
        val shadeColor = if (isNight) NoteColors.DARK_COLOR
        else NoteColors.LIGHT_COLOR

        with(binding) {
            mbNoteEditColorPink.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.PINK])
            )
            mbNoteEditColorPurple.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.PURPLE])
            )
            mbNoteEditColorIndigo.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.INDIGO])
            )
            mbNoteEditColorGreen.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.GREEN])
            )
            mbNoteEditColorOrange.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.ORANGE])
            )
            mbNoteEditColorBrown.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.BROWN])
            )
            mbNoteEditColorGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.GRAY])
            )
            mbNoteEditColorBlueGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.BLUE_GRAY])
            )
        }
    }

    private fun getCutCopyString() {
        val extractedString = getExtractedString()
        extractedString?.let {
            getCutString()
            copySelToClip(extractedString)
        }
    }

    private fun getCopyString() {
        val extractedString = getExtractedString()
        extractedString?.let {
            copySelToClip(extractedString)
        }
    }

    private fun getEditColorNote() {
        val isShow = viewModel.isShowColorFabs.value
        val buttonSaveOption = viewModel.getSaveOption.value
        if (isShow == null || !isShow) {
            viewModel.setShowColorFabs(true)
            if (buttonSaveOption == true) viewModel.setSaveOption()
        } else {
            viewModel.setShowColorFabs(false)
        }
    }

    // validating entry in note fields, preparing to exit fragment
    private fun exitFragment() {
        val title = binding.etNoteEditTitle.text.toString()
        val itself = binding.etNoteEditItself.text.toString()
        when (workingMode) {
            MODE_ADD -> viewModel.addNote(title, itself)
            MODE_EDIT -> viewModel.editNote(title, itself)
        }
        viewModel.setSaveOption()
    }

    // select all text note itself
    private fun selectAll() {
        val text = binding.etNoteEditItself.text
        text?.let {
            val finish = text.length
            binding.etNoteEditItself.setSelection(0, finish)
        }
    }

    private fun getExtractedString(): String? {
        val someView = requireActivity().currentFocus
        val view = if (someView == binding.etNoteEditTitle) binding.etNoteEditTitle
        else binding.etNoteEditItself

        val startIndex = view.selectionStart
        val endIndex = view.selectionEnd
        return if (startIndex != endIndex) {
            val string = view.text
            string?.substring(startIndex, endIndex)
        } else {
            showSnakebar(getString(R.string.select_text_to_copy))
            null
        }
    }

    private fun getCutString() {
        val someView = requireActivity().currentFocus
        val view = if (someView == binding.etNoteEditTitle) binding.etNoteEditTitle
        else binding.etNoteEditItself

        val startIndex = view.selectionStart
        val endIndex = view.selectionEnd

        val string = view.text
        val startString = string?.substring(0, startIndex)
        val endString = string?.substring(endIndex)
        val fullString = startString + endString
        val editableString = Editable.Factory.getInstance().newEditable(fullString)

        view.text?.clear()
        view.text = editableString
        view.setSelection(startIndex)
    }

    private fun copySelToClip(extractedString: String) {
        val clipboardManager = requireActivity()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(null, extractedString)
        clipboardManager.setPrimaryClip(clip)
        showSnakebar(getString(R.string.sel_text_copied_clipboard))
    }

    private fun setCopyFromClip() {
        val clipboardManager = requireActivity()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = clipboardManager.primaryClip
        val item = data?.getItemAt(0)
        if (item != null) {
            val copyString = item.text.toString()

            val someView = requireActivity().currentFocus
            val view = if (someView == binding.etNoteEditTitle) binding.etNoteEditTitle
            else binding.etNoteEditItself

            val startIndex = view.selectionStart
            val endIndex = view.selectionEnd

            val string = view.text
            val startString = string?.substring(0, startIndex)
            val endString = string?.substring(endIndex)
            val fullString = startString + copyString + endString

            val newCursorPosition = startIndex + copyString.length

            view.text?.clear()
            view.text = Editable.Factory.getInstance().newEditable(fullString)
            view.setSelection(newCursorPosition)
        } else {
            showSnakebar(getString(R.string.nothing_on_clipboard))
        }
    }

    private fun observeIsShowColorButtons() {
        viewModel.isShowColorFabs.observe(viewLifecycleOwner) {
            if (it) {
                showColorButtons()
            } else {
                hideColorButtons()
            }
        }
    }

    private fun showColorButtons() {
        behaviorBottomAppBar.slideDown(binding.babNoteEdit)
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_HIDDEN) {
            behaviorColorButtons.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun hideColorButtons() {
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_EXPANDED) {
            behaviorColorButtons.state = BottomSheetBehavior.STATE_HIDDEN
        }
        behaviorBottomAppBar.slideUp(binding.babNoteEdit)
    }

    private fun launchModes() {
        when (workingMode) {
            MODE_ADD -> launchAddMode()
            MODE_EDIT -> launchEditMode()
        }
    }

    private fun launchAddMode() {
        viewModel.setTimeStamp(Date().time)
        viewModel.setIsLock(false)

        with(binding) {

            tvNoteEditFullDate.text = TimeUtils.getFullDate(viewModel.timeStamp)

            etNoteEditItself.requestFocus()
            showSoftKeyboard()
        }
    }

    private fun launchEditMode() {
        getNoteEntry()
    }

    private fun observeNote() {
        viewModel.note.observe(viewLifecycleOwner) {

            viewModel.setIsLock(it.isLocked)
            viewModel.setColorIndex(it.colorIndex)

            with(binding) {
                // fill fields with text
                etNoteEditTitle.text = Editable.Factory.getInstance().newEditable(it.titleNote)
                etNoteEditItself.text = Editable.Factory.getInstance().newEditable(it.itselfNote)
                tvNoteEditFullDate.text = TimeUtils.getFullDate(it.timeStamp)
                // set focus to EditText and move cursor to end
                etNoteEditItself.requestFocus()
                etNoteEditItself.setSelection(etNoteEditItself.text.toString().length)

                showSoftKeyboard()
            }
        }
    }

    private fun observeIsLock() {
        val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
        else R.drawable.ic_lock_black_36dp
        val imgUnlock = if (isNight) R.drawable.ic_lock_open_white_36dp
        else R.drawable.ic_lock_open_black_36dp

        viewModel.isLock.observe(viewLifecycleOwner) {
            with(binding) {
                if (it) {
                    tvNoteEditLock.text = requireContext().getText(R.string.unlock_note)
                    ivNoteEditLock.setImageResource(imgLock)
                } else {
                    tvNoteEditLock.text = requireContext().getText(R.string.lock_note)
                    ivNoteEditLock.setImageResource(imgUnlock)
                }
            }
        }
    }

    private fun observeColorIndex() {
        viewModel.colorIndex.observe(viewLifecycleOwner) {
            val colorNote =
                if (isNight) NoteColors.noteColor[NoteColors.DARK_COLOR][it]
                else NoteColors.noteColor[NoteColors.LIGHT_COLOR][it]
            with(binding) {
                cvNoteEditTitle.setBackgroundResource(colorNote)
                cvNoteEditLock.setBackgroundResource(colorNote)
                cvNoteEditCreate.setBackgroundResource(colorNote)
                cvNoteEditItself.setBackgroundResource(colorNote)
            }
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(WORKING_MODE)) {
            throw RuntimeException("Param working mode is absent")
        }
        val mode = args.getString(WORKING_MODE)
        if (mode != MODE_ADD && mode != MODE_EDIT) {
            throw RuntimeException("Unknown working mode $mode")
        }
        workingMode = mode

        if (workingMode == MODE_EDIT) {
            if (!args.containsKey(TIME_STAMP)) {
                throw RuntimeException("Arguments don't contains timeStamp")
            }
            viewModel.setTimeStamp(args.getLong(TIME_STAMP))
        }
    }

    private fun retryNoteListFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0
        )
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
    }

    private fun showSoftKeyboard() {
        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etNoteEditItself, 0)
    }

    private fun Fragment.hideSoftKeyboard() {
        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun showSaveFab() {
        val animShowFabSave = AnimationUtils.loadAnimation(context, R.anim.fab_save_show)
        with(binding.fabNoteEditSave) {
            val lLayoutParams = layoutParams as CoordinatorLayout.LayoutParams
            lLayoutParams.marginEnd -= (width * 0.25).toInt()
            lLayoutParams.bottomMargin += (height * 1.7).toInt()
            layoutParams = lLayoutParams
            startAnimation(animShowFabSave)
            isClickable = true
        }
    }

    private fun hideSaveFab() {
        val anim = if (viewModel.isShowColorFabs.value == false) R.anim.fab_save_hide
        else R.anim.fab_save_other_hide

        val animHideFabSave =
            AnimationUtils.loadAnimation(context, anim)

        with(binding.fabNoteEditSave) {
            val lLayoutParams = layoutParams as CoordinatorLayout.LayoutParams
            lLayoutParams.marginEnd += (width * 0.25).toInt()
            lLayoutParams.bottomMargin -= (height * 1.7).toInt()
            layoutParams = lLayoutParams
            startAnimation(animHideFabSave)
            isClickable = false
        }
    }

    private fun showNoSaveFab() {
        val animShowFabSave =
            AnimationUtils.loadAnimation(context, R.anim.fab_no_save_show)
        with(binding.fabNoteEditNotSave) {
            val lLayoutParams = layoutParams as CoordinatorLayout.LayoutParams
            lLayoutParams.marginEnd += (width * 1.5).toInt()
            lLayoutParams.bottomMargin += (height * 1)
            layoutParams = lLayoutParams
            startAnimation(animShowFabSave)
            isClickable = true
        }
    }

    private fun hideNoSaveFab() {
        val anim = if (viewModel.isShowColorFabs.value == false) R.anim.fab_no_save_hide
        else R.anim.fab_no_save_other_hide

        val animHideFabSave =
            AnimationUtils.loadAnimation(context, anim)

        with(binding.fabNoteEditNotSave) {
            val lLayoutParams = layoutParams as CoordinatorLayout.LayoutParams
            lLayoutParams.marginEnd -= (width * 1.5).toInt()
            lLayoutParams.bottomMargin -= (height * 1)
            layoutParams = lLayoutParams
            startAnimation(animHideFabSave)
            isClickable = false
        }
    }

    private fun showSnakebar(message: String) {
        val icon = if (isNight) R.drawable.ic_information_variant_black_48dp
        else R.drawable.ic_information_variant_white_48dp

        val snackBar = Snackbar.make(
            requireActivity().findViewById(R.id.main_container),
            message,
            Snackbar.LENGTH_LONG
        )

        val snackBarView = snackBar.view
        val snackBarText = snackBarView.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        snackBarText.setCompoundDrawablesWithIntrinsicBounds(
            icon, 0, 0, 0
        )
        snackBarText.compoundDrawablePadding = 15
        snackBarText.gravity = Gravity.CENTER
        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackBar.anchorView = binding.fabNoteEditExit
        snackBar.show()
    }

    private fun testBiometricSuccess() {

        if (biometricVerification.readinessCheckBiometric(::showSnakebar))
            viewModel.setIsLock(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        const val NAME = "note_edit_fragment"

        private const val TIME_STAMP = "time_stamp"
        private const val MODE_ADD = "add_mode"
        private const val MODE_EDIT = "edit_mode"
        private const val WORKING_MODE = "working_mode"
        private const val MODE_UNKNOWN = ""

        @JvmStatic
        fun newInstanceEditMode(timeStamp: Long) =
            NoteEditFragment().apply {
                arguments = Bundle().apply {
                    putString(WORKING_MODE, MODE_EDIT)
                    putLong(TIME_STAMP, timeStamp)
                }
            }

        @JvmStatic
        fun newInstanceAddMode() = NoteEditFragment().apply {
            arguments = Bundle().apply {
                putString(WORKING_MODE, MODE_ADD)
            }
        }
    }
}