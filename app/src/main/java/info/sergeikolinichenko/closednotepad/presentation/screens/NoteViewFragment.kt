package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteViewBinding
import info.sergeikolinichenko.closednotepad.presentation.utils.EntriesColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteView
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteViewFactory

private const val TIME_STAMP = "time_stamp"

class NoteEntryViewFragment : Fragment() {

    private val viewModelFactory by lazy {
        ViewModelNoteViewFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteView::class.java]
    }

    private var isNight = false

    private var _timeStamp: Long? = null
    private val timeStamp: Long
        get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var _binding: FragmentNoteViewBinding? = null
    private val binding: FragmentNoteViewBinding
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
        _binding = FragmentNoteViewBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBackPressed()
        initBottomAppBar()
        initFab()
        observeNoteEntry()
        onScrollChangeListener()
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
        if (isNight) {
            with(binding) {
                ibNoteEntryViewDelete.setImageResource(
                    R.drawable.ic_delete_white_36dp
                )
                ibNoteEntryViewSend.setImageResource(
                    R.drawable.ic_send_white_36dp
                )
                ibNoteEntryViewCopy.setImageResource(
                    R.drawable.ic_content_copy_white_36dp
                )
                ibNoteEntryViewEdit.setImageResource(
                    R.drawable.ic_application_edit_outline_white_36dp
                )
            }
        } else {
            with(binding) {
                ibNoteEntryViewDelete.setImageResource(
                    R.drawable.ic_delete_black_36dp
                )
                ibNoteEntryViewSend.setImageResource(
                    R.drawable.ic_send_black_36dp
                )
                ibNoteEntryViewCopy.setImageResource(
                    R.drawable.ic_content_copy_black_36dp
                )
                ibNoteEntryViewEdit.setImageResource(
                    R.drawable.ic_application_edit_outline_black_36dp
                )
            }
        }
        with(binding) {
            ibNoteEntryViewEdit.setOnClickListener {
                launchNoteEntryEditFragment()
            }
        }
    }

    private fun initFab() {
        binding.fabNoteEntryView.setOnClickListener {
            retryToNoteListFragment()
        }
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    retryToNoteListFragment()
                }

            }
        )
    }

    private fun observeNoteEntry() {
        viewModel.noteEntry.observe(viewLifecycleOwner) {

            val colorEntry =
                if (isNight) EntriesColors.entriesColor[EntriesColors.DARK_COLOR][it.colorIndex]
                else EntriesColors.entriesColor[EntriesColors.LIGHT_COLOR][it.colorIndex]

            val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
            else R.drawable.ic_lock_black_36dp

            with(binding) {
//                clNoteView.setBackgroundResource(colorEntry)
                //               cvNoteView.setBackgroundResource(colorEntry)
                cvNoteViewItselfNote.setBackgroundResource(colorEntry)
                cvNoteViewTitle.setBackgroundResource(colorEntry)
                ivNoteViewLock.setImageResource(imgLock)
                tvNoteViewFulldate.text = TimeUtils.getFullDate(it.timeStamp)
                tvNoteViewTitleNote.text = it.titleNote
                tvNoteViewItselfNote.text = it.itselfNote
                if (it.isLocked) {
                    binding.ivNoteViewLock.visibility = View.VISIBLE
                } else {
                    binding.ivNoteViewLock.visibility = View.INVISIBLE
                }
            }
        }
        viewModel.getNoteEntry(timeStamp)
    }

    private fun onScrollChangeListener() {
        binding.svNoteView.viewTreeObserver.addOnScrollChangedListener {
            with(binding) {
                if (svNoteView.scrollY > 0) {
                    babNoteView.performHide()
                    fabNoteEntryView.hide()
                } else {
                    babNoteView.performShow()
                    fabNoteEntryView.show()
                }
            }
        }
    }

    private fun retryToNoteListFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0
        )
    }

    private fun launchNoteEntryEditFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
            .replace(R.id.main_container, NoteEntryEditFragment.newInstanceEditMode(timeStamp))
            .addToBackStack(NoteEntryEditFragment.NAME)
            .commit()
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