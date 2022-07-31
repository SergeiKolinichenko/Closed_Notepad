package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteViewBinding
import info.sergeikolinichenko.closednotepad.presentation.utils.EntriesColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteView
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteViewFactory

private const val TIME_STAMP = "time_stamp"

class NoteViewFragment : Fragment() {

    private val viewModelFactory by lazy {
        ViewModelNoteViewFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteView::class.java]
    }

    private lateinit var sendNoteTo: SendNoteTo
    private var isNight = false

    private var _timeStamp: Long? = null
    private val timeStamp: Long
        get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var _binding: FragmentNoteViewBinding? = null
    private val binding: FragmentNoteViewBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteViewBinding equals null")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SendNoteTo) {
            sendNoteTo = context
        } else {
            throw RuntimeException(
                "There is no implementation of the interface SendNoteTo" +
                        " in the activity $context"
            )
        }
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
        observeCopyContent()
        observeSendNoteTo()
        observeViewToast()
        observeDeleteNote()
        observeEndUsingFragment()
        observeNoteToNoteListFrag()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.svNoteView.viewTreeObserver.removeOnGlobalLayoutListener(null)
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
                ibNoteViewDeleteNote.setImageResource(
                    R.drawable.ic_delete_white_36dp
                )
                ibNoteViewSendNote.setImageResource(
                    R.drawable.ic_send_white_36dp
                )
                ibNoteViewCopyContent.setImageResource(
                    R.drawable.ic_content_copy_white_36dp
                )
                ibNoteViewEditNote.setImageResource(
                    R.drawable.ic_application_edit_outline_white_36dp
                )
            }
        } else {
            with(binding) {
                ibNoteViewDeleteNote.setImageResource(
                    R.drawable.ic_delete_black_36dp
                )
                ibNoteViewSendNote.setImageResource(
                    R.drawable.ic_send_black_36dp
                )
                ibNoteViewCopyContent.setImageResource(
                    R.drawable.ic_content_copy_black_36dp
                )
                ibNoteViewEditNote.setImageResource(
                    R.drawable.ic_application_edit_outline_black_36dp
                )
            }
        }
        with(binding) {
            ibNoteViewEditNote.setOnClickListener {
                launchNoteEntryEditFragment()
            }
            ibNoteViewCopyContent.setOnClickListener {
                viewModel.pushButtonCopyContent()
            }
            ibNoteViewSendNote.setOnClickListener {
                viewModel.pushButtonSendNote()
            }
            ibNoteViewDeleteNote.setOnClickListener {
                viewModel.pushButtonDelete()
            }
        }
    }

    private fun observeCopyContent() {
        viewModel.buttonCopyContent.observe(viewLifecycleOwner) {
            val textToCopy = binding.tvNoteViewItselfNote.text
            val clipboardManager = requireActivity()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(null, textToCopy)
            clipboardManager.setPrimaryClip(clip)
            viewModel.showToast(getString(R.string.copy_text_clipboard))
        }
    }

    private fun observeSendNoteTo() {
        viewModel.buttonSendNote.observe(viewLifecycleOwner) {
            val txtTitleNote = binding.tvNoteViewTitleNote.toString()
            val txtItselfNote = binding.tvNoteViewItselfNote.toString()
            sendNoteTo.sendNoteTO(txtTitleNote, txtItselfNote)
        }
    }

    private fun observeDeleteNote() {
        viewModel.buttonDeleteNote.observe(viewLifecycleOwner) {
            Snackbar.make(
                requireActivity().findViewById(R.id.main_container),
                resources.getString(R.string.confirm_deletion_note),
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.delete_note) {
                    viewModel.removeNote()
                }
                .show()
        }
    }

    private fun observeNoteToNoteListFrag() {
        viewModel.noteToNoteListFrag.observe(viewLifecycleOwner){
            requireActivity().supportFragmentManager.setFragmentResult(
                NoteListFragment.BUNDLE_KEY_NOTE,
                bundleOf(NoteListFragment.BUNDLE_KEY_NOTE to it.timeStamp)
            )
            retryNoteListFragment()
        }
    }

    private fun initFab() {
        binding.fabNoteEntryView.setOnClickListener {
            viewModel.endUsingFragment()
        }
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    retryNoteListFragment()
                }

            }
        )
    }

    private fun observeNoteEntry() {
        viewModel.note.observe(viewLifecycleOwner) {

            val colorEntry =
                if (isNight) EntriesColors.entriesColor[EntriesColors.DARK_COLOR][it.colorIndex]
                else EntriesColors.entriesColor[EntriesColors.LIGHT_COLOR][it.colorIndex]

            val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
            else R.drawable.ic_lock_black_36dp

            with(binding) {
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
            if (_binding != null) {
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
    }

    private fun observeViewToast() {
        viewModel.toast.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun observeEndUsingFragment() {
        viewModel.endUsingFragment.observe(viewLifecycleOwner) {
            retryNoteListFragment()
        }
    }

    private fun retryNoteListFragment() {
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

    interface SendNoteTo {
        fun sendNoteTO(txtTitleNote: String, txtItselfNote: String)
    }

    companion object {

        const val NAME = "note_view_fragment"

        @JvmStatic
        fun newInstance(timeStamp: Long) =
            NoteViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(TIME_STAMP, timeStamp)
                }
            }
    }
}