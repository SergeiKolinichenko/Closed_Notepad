package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteEntryEditBinding
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEntryEdit
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEntryEditFactory

private const val TIME_STAMP = "time_stamp"
private const val MODE_ADD = "add_mode"
private const val MODE_EDIT = "edit_mode"
private const val WORKING_MODE = "working_mode"
private const val MODE_UNKNOWN = ""
private const val NO_TIME_STAMP = -1

/**
Editing element of notebook
create 28.07.2022 18:21 by Sergei Kolinichenko
 */

class NoteEntryEditFragment : Fragment() {

    private val viewModelFactory by lazy {
        ViewModelNoteEntryEditFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteEntryEdit::class.java]
    }

    private var _timeStamp: Long? = null
    private val timeStamp: Long
        get() = _timeStamp ?: throw RuntimeException("Attention: timeStamp equals null")

    private var _binding: FragmentNoteEntryEditBinding? = null
    private val binding: FragmentNoteEntryEditBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteEntryEditBinding equals null")

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
    ): View? {
        _binding = FragmentNoteEntryEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBackPressed()

        getNoteEntry()
        launchModes()
    }

    private fun getNoteEntry() {
        viewModel.getNoteEntry(timeStamp)
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    retryToEntryViewFragment()
                }

            }
        )
    }

    private fun launchModes() {
        when(workingMode) {
            MODE_ADD -> TODO()
            MODE_EDIT -> launchEditMode()
        }
    }

    private fun launchEditMode() {
        viewModel.noteEntry.observe(viewLifecycleOwner) {
            with(binding) {
                etTitleNoteEntryEdit.text = Editable.Factory.getInstance().newEditable(it.titleNote)
                etItselfNoteEntryEdit.text = Editable.Factory.getInstance().newEditable(it.itselfNote)
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
            _timeStamp = args.getLong(TIME_STAMP)
        }
    }

    private fun retryToEntryViewFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            NoteViewFragment.NAME,
            0
        )
    }

    private fun retryToNoteListFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0
        )
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        const val NAME = "note_entry_edit_fragment"

        @JvmStatic
        fun newInstanceEditMode(timeStamp: Long) =
            NoteEntryEditFragment().apply {
                arguments = Bundle().apply {
                    putString(WORKING_MODE, MODE_EDIT)
                    putLong(TIME_STAMP, timeStamp)
                }
            }

        @JvmStatic
        fun newInstanceAddMode() = NoteEntryEditFragment().apply {
            arguments = Bundle().apply {
                putString(WORKING_MODE, MODE_ADD)
            }
        }
    }
}