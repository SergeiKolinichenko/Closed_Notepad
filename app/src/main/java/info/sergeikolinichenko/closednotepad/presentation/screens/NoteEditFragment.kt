package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteEditBinding
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.noteedit.ViewModelNoteEdit
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.noteedit.ViewModelNoteEditFactory
import java.util.*

/**
Editing element of notebook
create 28.07.2022 18:21 by Sergei Kolinichenko
 */

class NoteEditFragment : Fragment() {

    private val viewModelFactory by lazy {
        ViewModelNoteEditFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteEdit::class.java]
    }

    private var _binding: FragmentNoteEditBinding? = null
    private val binding: FragmentNoteEditBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteEditBinding equals null")

    private var isNight = false
    private var workingMode: String = MODE_UNKNOWN

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
        initLockButton()
        initBackPressed()
        initBottomAppBar()
        initFabExit()
        initFabSave()
        initFabNoSave()
        initColorButtons()
        launchModes()
    }

    private fun getNoteEntry() {
        viewModel.getNoteEntry()
    }

    private fun observeRetryNoteListFrag() {
        viewModel.retryNoteListFrag.observe(viewLifecycleOwner) {
            hideSoftKeyboard()
            retryNoteListFragment()
        }
    }

    private fun observeGetSaveOption() {
        viewModel.getSaveOption.observe(viewLifecycleOwner) {
            showSaveFab()
            showNoSaveFab()
        }
    }

    private fun initLockButton() {
        binding.cvNoteEditLock.setOnClickListener {
            viewModel.changeIsLock()
        }
    }

    private fun initFabExit() {
        binding.fabNoteEditExit.setOnClickListener {
            val title = binding.etNoteEditTitle.text.toString()
            val itself = binding.etNoteEditItself.text.toString()
            when (workingMode) {
                MODE_ADD -> viewModel.addNote(title, itself)
                MODE_EDIT -> viewModel.editNote(title, itself)
            }
        }
    }

    private fun initFabSave() {
        binding.fabNoteEditSave.setOnClickListener {
            when (workingMode) {
                MODE_ADD -> viewModel.addNoteToBase()
                MODE_EDIT -> viewModel.editNoteToBase()
            }
//            hideSaveFab()
//            hideNoSaveFab()
        }
    }

    private fun initFabNoSave() {
        binding.fabNoteEditNotSave.setOnClickListener {
            viewModel.retryNoteListFrag()
        }
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val title = binding.etNoteEditTitle.text.toString()
                    val itself = binding.etNoteEditItself.text.toString()
                    when (workingMode) {
                        MODE_ADD -> viewModel.addNote(title, itself)
                        MODE_EDIT -> viewModel.editNote(title, itself)
                    }
                }

            }
        )
    }

    private fun initBottomAppBar() {
        with(binding) {
            if (isNight) {
                ibNoteEditCut.setImageResource(R.drawable.ic_content_cut_white_36dp)
                ibNoteEditCopy.setImageResource(R.drawable.ic_content_copy_white_36dp)
                ibNoteEditPaste.setImageResource(R.drawable.ic_content_paste_white_36dp)
                ibNoteEditPallet.setImageResource(R.drawable.ic_palette_white_36dp)
            } else {
                ibNoteEditCut.setImageResource(R.drawable.ic_content_cut_black_36dp)
                ibNoteEditCopy.setImageResource(R.drawable.ic_content_copy_black_36dp)
                ibNoteEditPaste.setImageResource(R.drawable.ic_content_paste_black_36dp)
                ibNoteEditPallet.setImageResource(R.drawable.ic_palette_black_36dp)
            }
        }

        with(binding) {
            ibNoteEditCut.setOnClickListener {
                val extractedString = getExtractedStr()
                extractedString?.let {
                    getCutString()
                    copySelToClip(extractedString)
                }
            }
            ibNoteEditCopy.setOnClickListener {
                val extractedString = getExtractedStr()
                extractedString?.let {
                    copySelToClip(extractedString)
                }
            }
            ibNoteEditPaste.setOnClickListener {
                setCopyFromClip()
            }
            ibNoteEditPallet.setOnClickListener {
                val isShow = viewModel.isShowColorFabs.value
                if (isShow == null || !isShow) {
                    viewModel.setShowColorFabs(true)
                } else {
                    viewModel.setShowColorFabs(false)
                }
            }
        }
    }

    private fun getExtractedStr(): String? {
        val startIndex = binding.etNoteEditItself.selectionStart
        val endIndex = binding.etNoteEditItself.selectionEnd
        return if (startIndex != endIndex) {
            val string = binding.etNoteEditItself.text
            string?.substring(startIndex, endIndex)
        } else {
            showToast(getString(R.string.select_text_to_copy))
            null
        }
    }

    private fun getCutString() {
        val startIndex = binding.etNoteEditItself.selectionStart
        val endIndex = binding.etNoteEditItself.selectionEnd

        val string = binding.etNoteEditItself.text
        val startString = string?.substring(0, startIndex)
        val endString = string?.substring(endIndex)
        val fullString = startString + endString
        val editableString = Editable.Factory.getInstance().newEditable(fullString)

        binding.etNoteEditItself.text?.clear()
        binding.etNoteEditItself.text = editableString
        binding.etNoteEditItself.setSelection(startIndex)
    }

    private fun copySelToClip(extractedString: String) {
        val clipboardManager = requireActivity()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(null, extractedString)
        clipboardManager.setPrimaryClip(clip)
        showToast(getString(R.string.sel_text_copied_clipboard))
    }

    private fun setCopyFromClip() {
        val clipboardManager = requireActivity()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = clipboardManager.primaryClip
        val item = data?.getItemAt(0)
        val copyString = item?.text.toString()

        val startIndex = binding.etNoteEditItself.selectionStart
        val endIndex = binding.etNoteEditItself.selectionEnd

        val string = binding.etNoteEditItself.text
        val startString = string?.substring(0, startIndex)
        val endString = string?.substring(endIndex)
        val fullString = startString + copyString + endString

        val newCursorPosition = startIndex + copyString.length

        binding.etNoteEditItself.text?.clear()
        binding.etNoteEditItself.text = Editable.Factory.getInstance().newEditable(fullString)
        binding.etNoteEditItself.setSelection(newCursorPosition)
    }

    private fun initColorButtons() {
        val shadeColor = if (isNight) NoteColors.DARK_COLOR
        else NoteColors.LIGHT_COLOR

        with(binding) {
            fabNoteEditColorPink.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.PINK])
            )
            fabNoteEditColorPurple.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.PURPLE])
            )
            fabNoteEditColorIndigo.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.INDIGO])
            )
            fabNoteEditColorGreen.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.GREEN])
            )
            fabNoteEditColorOrange.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.ORANGE])
            )
            fabNoteEditColorBrown.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.BROWN])
            )
            fabNoteEditColorGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.GRAY])
            )
            fabNoteEditColorBlueGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.BLUE_GRAY])
            )
            fabNoteEditColorPink.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PINK)
            }
            fabNoteEditColorPurple.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PURPLE)
            }
            fabNoteEditColorIndigo.setOnClickListener {
                viewModel.setColorIndex(NoteColors.INDIGO)
            }
            fabNoteEditColorGreen.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GREEN)
            }
            fabNoteEditColorOrange.setOnClickListener {
                viewModel.setColorIndex(NoteColors.ORANGE)
            }
            fabNoteEditColorBrown.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BROWN)
            }
            fabNoteEditColorGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GRAY)
            }
            fabNoteEditColorBlueGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BLUE_GRAY)
            }
        }
    }

    private fun observeIsShowColorButtons() {
        viewModel.isShowColorFabs.observe(viewLifecycleOwner) {
            if (it) {
                with(binding) {
                    showColorButton(fabNoteEditColorPink)
                    showColorButton(fabNoteEditColorPurple)
                    showColorButton(fabNoteEditColorIndigo)
                    showColorButton(fabNoteEditColorGreen)
                    showColorButton(fabNoteEditColorOrange)
                    showColorButton(fabNoteEditColorBrown)
                    showColorButton(fabNoteEditColorGray)
                    showColorButton(fabNoteEditColorBlueGray)
                }
            } else {
                with(binding) {
                    hideColorButton(fabNoteEditColorPink)
                    hideColorButton(fabNoteEditColorPurple)
                    hideColorButton(fabNoteEditColorIndigo)
                    hideColorButton(fabNoteEditColorGreen)
                    hideColorButton(fabNoteEditColorOrange)
                    hideColorButton(fabNoteEditColorBrown)
                    hideColorButton(fabNoteEditColorGray)
                    hideColorButton(fabNoteEditColorBlueGray)
                }
            }
        }
    }

    private fun showColorButton(view: View) {
        view.animate().alpha(1F).scaleX(1.2F).scaleY(1.2F).setDuration(500).start()
        view.visibility = View.VISIBLE
        view.isClickable = true
    }

    private fun hideColorButton(view: View) {
        view.animate().alpha(0F).scaleX(0F).scaleY(0F).setDuration(500).start()
        view.isClickable = false
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

        viewModel.setColorIndex(NoteColors.BLUE_GRAY) // TODO create load colorIndex from preferences

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
                if (isNight) NoteColors.entriesColor[NoteColors.DARK_COLOR][it]
                else NoteColors.entriesColor[NoteColors.LIGHT_COLOR][it]
            with(binding) {
                cvNoteEditTitle.setBackgroundResource(colorNote)
                cvNoteEditLock.setBackgroundResource(colorNote)
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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