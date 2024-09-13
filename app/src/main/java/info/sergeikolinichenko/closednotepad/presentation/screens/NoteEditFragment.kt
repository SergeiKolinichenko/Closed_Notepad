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
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteEditBinding
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.NotesApp
import info.sergeikolinichenko.closednotepad.presentation.stateful.*
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.utils.readinessCheckBiometric
import info.sergeikolinichenko.closednotepad.presentation.utils.showSnakebar
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEdit
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEdit.Companion.EMPTY_STRING
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEdit.Companion.MAX_TITLE_LENGTH
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNotesFactory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 28.07.2022 at 18:21 (GMT+3) **/

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

    private val component by lazy { (requireActivity().application as NotesApp).component }

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
        observeStateNoteEdit()
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

            val isLock = binding.tvNoteEditLock.text == resources.getString(R.string.lock_note)

            if (isLock) {
                // set unlock note
                setLockNote(NOTE_UNLOCK)
            } else {
                // check the possibility of locking the screen
                testBiometricSuccess()
            }
        }
    }

    private fun initFabExit() {
        binding.fabNoteEditExit.setOnClickListener {
            val title = binding.etNoteEditTitle.text.toString()
            val itself = binding.etNoteEditItself.text.toString()
            val isLock = binding.tvNoteEditLock.text == resources.getString(R.string.lock_note)
            when (workingMode) {
                MODE_ADD -> viewModel.addNote(title, itself, isLock)
                MODE_EXTRA_ADD -> viewModel.addNote(title, itself, isLock)
                MODE_EDIT -> viewModel.editNote(title, itself, isLock)
            }
            viewModel.switchExtraFABs()
        }
    }

    private fun initFabSave() {
        binding.fabNoteEditSave.setOnClickListener {
            when (workingMode) {
                MODE_ADD -> viewModel.getAddNoteDatabase()
                MODE_EXTRA_ADD -> viewModel.getExtraAddNoteDatabase()
                MODE_EDIT -> viewModel.getEditNoteDatabase()
            }
        }
        binding.fabNoteEditSave.isClickable = false
    }

    private fun initFabNoSave() {
        binding.fabNoteEditNotSave.setOnClickListener {
            when(workingMode) {
                MODE_ADD -> viewModel.retryNoteListFragment()
                MODE_EXTRA_ADD -> viewModel.getNoteListFragment()
                MODE_EDIT -> viewModel.retryNoteListFragment()
            }
        }
        binding.fabNoteEditNotSave.isClickable = false
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.switchExtraFABs()
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
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_HIDDEN) {
            viewModel.setShowColorButtons()
            if (binding.fabNoteEditSave.isClickable) viewModel.hideExtraFABs()
        } else {
            viewModel.setHideColorButtons()
        }
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
            showSnakebar(
                requireActivity().findViewById(R.id.main_container),
                binding.fabNoteEditExit,
                isNight,
                getString(R.string.select_text_to_copy)
            )
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
        showSnakebar(
            requireActivity().findViewById(R.id.main_container),
            binding.fabNoteEditExit,
            isNight,
            getString(R.string.sel_text_copied_clipboard)
        )
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

            var newCursorPosition = startIndex + copyString.length
            if (view == binding.etNoteEditTitle && newCursorPosition > MAX_TITLE_LENGTH) {
                newCursorPosition = MAX_TITLE_LENGTH
            }


            view.text?.clear()
            view.text = Editable.Factory.getInstance().newEditable(fullString)
            view.setSelection(newCursorPosition)
        } else {
            showSnakebar(
                requireActivity().findViewById(R.id.main_container),
                binding.fabNoteEditExit,
                isNight,
                getString(R.string.nothing_on_clipboard)
            )
        }
    }

    private fun showColorButtons() {
        lifecycleScope.launch {
            hideSoftKeyboard()
            delay(100)
            if (behaviorColorButtons.state == BottomSheetBehavior.STATE_HIDDEN) {
                behaviorColorButtons.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun hideColorButtons() {
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_EXPANDED) {
            behaviorColorButtons.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun launchModes() {
        when (workingMode) {
            MODE_ADD -> launchAddMode()
            MODE_EDIT -> launchEditMode()
            MODE_EXTRA_ADD -> launchExtraAddMode()
        }
    }

    private fun launchAddMode() {
        viewModel.setTimeStamp(Date().time)
        setLockNote(NOTE_UNLOCK)
        viewModel.getDefaultColorIndex()
        viewModel.getAddNoteMode()

        binding.etNoteEditItself.requestFocus()
        showSoftKeyboard()
    }

    private fun launchExtraAddMode() {
        viewModel.setTimeStamp(Date().time)
        setLockNote(NOTE_UNLOCK)
        viewModel.getDefaultColorIndex()
        viewModel.getExtraAddNoteMode()
    }

    private fun launchEditMode() {
        viewModel.getEditNoteMode()
    }

    private fun setNote(note: Note) {
        setLockNote(note.isLocked)
        viewModel.setColorIndex(note.colorIndex)

        // fill fields with text
        binding.etNoteEditTitle.text = Editable.Factory.getInstance().newEditable(note.titleNote)
        binding.etNoteEditItself.text = Editable.Factory.getInstance().newEditable(note.itselfNote)
        binding.tvNoteEditFullDate.text = TimeUtils.getFullDate(note.timeStamp)
        // set focus to EditText and move cursor to end
        binding.etNoteEditItself.requestFocus()
        binding.etNoteEditItself.setSelection(binding.etNoteEditItself.text.toString().length)

        showSoftKeyboard()
    }

    private fun observeStateNoteEdit() {
        viewModel.stateNoteEdit.observe(viewLifecycleOwner) { it ->
            when (it) {
                is NoteEditNote -> {
                    it.note?.let {
                        setNote(it)
                        setColorIndex(it.colorIndex)
                    }
                }
                is ColorIndex -> setColorIndex(it.index)
                ShowExtraFABs -> showExtraFABs()
                HideExtraFABs -> hideExtraFABs()
                Lock -> setLockNote(NOTE_LOCK)
                Unlock -> setLockNote(NOTE_UNLOCK)
                RetryNoteListFragment -> retryNoteListFragment()
                GetNoteListFragment -> launchNoteListFragment()
                ShowColorButtonsNoteEdit -> showColorButtons()
                HideColorButtonsNoteEdit -> hideColorButtons()
            }
        }
    }

    private fun setLockNote(isLock: Boolean) {
        val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
        else R.drawable.ic_lock_black_36dp
        val imgUnlock = if (isNight) R.drawable.ic_lock_open_white_36dp
        else R.drawable.ic_lock_open_black_36dp

        with(binding) {
            if (isLock) {
                tvNoteEditLock.text = requireContext().getText(R.string.lock_note)
                ivNoteEditLock.setImageResource(imgLock)
            } else {
                tvNoteEditLock.text = requireContext().getText(R.string.unlock_note)
                ivNoteEditLock.setImageResource(imgUnlock)
            }
        }
    }

    private fun setColorIndex(colorIndex: Int) {
        val colorNote =
            if (isNight) NoteColors.noteColor[NoteColors.DARK_COLOR][colorIndex]
            else NoteColors.noteColor[NoteColors.LIGHT_COLOR][colorIndex]
        with(binding) {
            cvNoteEditTitle.setBackgroundResource(colorNote)
            cvNoteEditLock.setBackgroundResource(colorNote)
            cvNoteEditCreate.setBackgroundResource(colorNote)
            cvNoteEditItself.setBackgroundResource(colorNote)
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(WORKING_MODE)) {
            throw RuntimeException("Param working mode is absent")
        }
        val mode = args.getString(WORKING_MODE)
        if (mode != MODE_ADD && mode != MODE_EDIT && mode != MODE_EXTRA_ADD) {
            throw RuntimeException("Unknown working mode $mode")
        }
        workingMode = mode

        if (workingMode == MODE_EDIT) {
            if (!args.containsKey(TIME_STAMP)) {
                throw RuntimeException("Arguments don't contains timeStamp")
            }
            viewModel.setTimeStamp(args.getLong(TIME_STAMP))
        } else if (workingMode == MODE_EXTRA_ADD) {
            viewModel.setNoteTitle(args.getString(NOTE_TITLE, EMPTY_STRING))
            viewModel.setNoteItself(args.getString(NOTE_ITSELF, EMPTY_STRING))
        }
    }

    private fun retryNoteListFragment() {
        lifecycleScope.launch {
            hideSoftKeyboard()
        }
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0
        )
    }

    private fun launchNoteListFragment() {
        requireActivity().supportFragmentManager.popBackStack()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_container,
                NoteListFragment.newInstance(), NoteListFragment.NAME
            )
            .addToBackStack(
                NoteListFragment.NAME
            )
            .commit()
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
    }

    private fun showExtraFABs() {
        showSaveFab()
        showNoSaveFab()
    }

    private fun hideExtraFABs() {
        hideSaveFab()
        hideNoSaveFab()
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
        val anim = if (
            behaviorColorButtons.state == BottomSheetBehavior.STATE_HIDDEN
        ) R.anim.fab_save_hide
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
        val anim = if (
            behaviorColorButtons.state == BottomSheetBehavior.STATE_HIDDEN
        ) R.anim.fab_no_save_hide
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

    private fun testBiometricSuccess() {

        val check = readinessCheckBiometric(
            this,
            BiometricManager.from(requireActivity().application),
            binding.fabNoteEditExit,
            isNight,
            ::showSnakebar
        )
        if (check) setLockNote(NOTE_LOCK)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        lifecycleScope.cancel()
    }

    companion object {

        const val NAME = "note_edit_fragment"

        private const val TIME_STAMP = "time_stamp"
        private const val MODE_ADD = "add_mode"
        private const val MODE_EDIT = "edit_mode"
        private const val MODE_EXTRA_ADD = "MODE_EXTRA_ADD"
        private const val WORKING_MODE = "working_mode"
        private const val NOTE_TITLE = "NOTE_TITLE"
        private const val NOTE_ITSELF = "NOTE_ITSELF"
        private const val MODE_UNKNOWN = ""
        private const val NOTE_UNLOCK = false
        private const val NOTE_LOCK = true


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

        @JvmStatic
        fun newInstanceExtraAddMode(
            noteTitle: String,
            noteItself: String
        ) = NoteEditFragment().apply {
            arguments = Bundle().apply {
                putString(WORKING_MODE, MODE_EXTRA_ADD)
                putString(NOTE_TITLE, noteTitle)
                putString(NOTE_ITSELF, noteItself)
            }
        }
    }
}