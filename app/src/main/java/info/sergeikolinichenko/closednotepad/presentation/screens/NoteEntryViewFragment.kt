package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteEntryBinding
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils

private const val TIME_STAMP = "time_stamp"

class NoteEntryViewFragment : Fragment() {

    private lateinit var noteEntry: NoteEntry

    private var _timeStamp: Long? = null
    private val timeStamp: Long
    get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var _binding: FragmentNoteEntryBinding? = null
    private val binding: FragmentNoteEntryBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteEntryBinding equals null")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNoteEntryBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        setEntryToViews()
        initBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(TIME_STAMP)) {
            throw RuntimeException("Arguments don't contains time_stamp")
        }
        _timeStamp = requireArguments().getLong(TIME_STAMP)
    }

    private fun initToolbar() {
        binding.toolBarNoteEntry.setBackgroundColor(noteEntry.colorIndex)
        var isLightTheme = true
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> isLightTheme = true
            Configuration.UI_MODE_NIGHT_UNDEFINED -> isLightTheme = true
            Configuration.UI_MODE_NIGHT_YES -> isLightTheme = false
        }
        if (isLightTheme) {
            binding.tvToolbarNoteEntry.setTextColor(Color.BLACK)
            binding.ivOutlineNoteEntry.setColorFilter(Color.BLACK)
        } else {
            binding.tvToolbarNoteEntry.setTextColor(Color.WHITE)
            binding.ivOutlineNoteEntry.setColorFilter(Color.WHITE)
        }
        binding.ivOutlineNoteEntry.setOnClickListener {
            retryNoteEntryFragment()
        }
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    retryNoteEntryFragment()
                }

            }
        )
    }

    private fun setEntryToViews() {
        binding.cvEntryNoteTop.setCardBackgroundColor(noteEntry.colorIndex)
        binding.cvToolbarNoteEntry.setCardBackgroundColor(noteEntry.colorIndex)
        binding.cvFragmentNoteEntryItself.setBackgroundColor(noteEntry.colorIndex)
        binding.tvFulldateEntry.text = TimeUtils.getFullDate(noteEntry.timeStamp)
        binding.tvToolbarNoteEntry.text = noteEntry.titleEntry
        binding.tvItselfEntryNote.text = noteEntry.itselfEntry
        if (noteEntry.isLocked) {
            binding.ivLockEntry.visibility = View.VISIBLE
        } else {
            binding.ivLockEntry.visibility = View.INVISIBLE
        }
    }

    private fun retryNoteEntryFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0)
    }

    companion object {

        const val NAME = "note_entry_fragment"

        @JvmStatic
        fun newInstance(timeStamp: Long) =
            NoteEntryViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(TIME_STAMP, timeStamp)
                }
            }
    }
}