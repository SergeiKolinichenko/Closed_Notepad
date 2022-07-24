package info.sergeikolinichenko.closednotepad.presentation.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteEntryViewBinding
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEntryView
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEntryViewFactory
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteList
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteListFactory

private const val TIME_STAMP = "time_stamp"

class NoteEntryViewFragment : Fragment() {

    private val viewModelFactory by lazy {
        ViewModelNoteEntryViewFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteEntryView::class.java]
    }

    private var _timeStamp: Long? = null
    private val timeStamp: Long
    get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var _binding: FragmentNoteEntryViewBinding? = null
    private val binding: FragmentNoteEntryViewBinding
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
        _binding = FragmentNoteEntryViewBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBackPressed()
        observeNoteEntry()
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

    private fun observeNoteEntry() {
        viewModel.noteEntry.observe(viewLifecycleOwner) {
            with(binding) {
                cvEntryNoteTop.setCardBackgroundColor(it.colorIndex)
                cvToolbarNoteEntryView.setCardBackgroundColor(it.colorIndex)
                cvNoteEntryItself.setBackgroundColor(it.colorIndex)
                tvFulldateEntry.text = TimeUtils.getFullDate(it.timeStamp)
                tvToolbarNoteEntry.text = it.titleEntry
                if (it.isLocked) {
                    binding.ivLockEntry.visibility = View.VISIBLE
                } else {
                    binding.ivLockEntry.visibility = View.INVISIBLE
                }
            }
        }
        viewModel.getNoteEntry(timeStamp)
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