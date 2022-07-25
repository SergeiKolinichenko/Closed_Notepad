package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteEntryViewBinding
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.presentation.utils.EntriesColors
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

    private var isNight = false

    private var _timeStamp: Long? = null
    private val timeStamp: Long
    get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var _binding: FragmentNoteEntryViewBinding? = null
    private val binding: FragmentNoteEntryViewBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteEntryBinding equals null")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
        isNightMode()
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
        initBottomAppBar()
        initFab()
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

    private fun initBottomAppBar() {
        if (isNight){
            with(binding){
                ibNoteEntryViewDelete.setImageResource(
                    R.drawable.ic_delete_white_36dp)
                ibNoteEntryViewSend.setImageResource(
                    R.drawable.ic_send_white_36dp)
                ibNoteEntryViewCopy.setImageResource(
                    R.drawable.ic_content_copy_white_36dp)
                ibNoteEntryViewEdit.setImageResource(
                    R.drawable.ic_application_edit_outline_white_36dp)
            }
        } else {
            with(binding) {
                ibNoteEntryViewDelete.setImageResource(
                    R.drawable.ic_delete_black_36dp)
                ibNoteEntryViewSend.setImageResource(
                    R.drawable.ic_send_black_36dp)
                ibNoteEntryViewCopy.setImageResource(
                    R.drawable.ic_content_copy_black_36dp)
                ibNoteEntryViewEdit.setImageResource(
                    R.drawable.ic_application_edit_outline_black_36dp)
            }
        }
    }

    private fun initFab() {
        binding.fabNoteEntryView.setOnClickListener {
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

    private fun observeNoteEntry() {
        viewModel.noteEntry.observe(viewLifecycleOwner) {

            val colorEntry = if (isNight) EntriesColors.entriesColor[EntriesColors.DARK_COLOR][it.colorIndex]
            else EntriesColors.entriesColor[EntriesColors.LIGHT_COLOR][it.colorIndex]

            val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
            else R.drawable.ic_lock_black_36dp

            with(binding) {
                clNoteEntryView.setBackgroundResource(colorEntry)
                cvNoteEntryItself.setBackgroundResource(colorEntry)
                cvEntryNoteItself.setBackgroundResource(colorEntry)
                cvNoteEntryViewDateLock.setBackgroundResource(colorEntry)
                ivLockEntryNoteView.setImageResource(imgLock)
                tvFulldateEntry.text = TimeUtils.getFullDate(it.timeStamp)
                tvToolbarNoteEntry.text = it.titleEntry
                tvItselfEntryNote.text = it.itselfEntry
                if (it.isLocked) {
                    binding.ivLockEntryNoteView.visibility = View.VISIBLE
                } else {
                    binding.ivLockEntryNoteView.visibility = View.INVISIBLE
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

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
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