package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnDetach
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteViewBinding
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.NotesApp
import info.sergeikolinichenko.closednotepad.presentation.stateful.EndUsing
import info.sergeikolinichenko.closednotepad.presentation.stateful.NoteViewNote
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.utils.showSnakebar
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteView
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNotesFactory
import javax.inject.Inject

class NoteViewFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelNotesFactory
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteView::class.java]
    }

    private lateinit var sendNoteTo: SendNoteTo // It is Intent.ACTION_SEND
    private var isNight = false

    private var _timeStamp: Long? = null
    private val timeStamp: Long
        get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var _binding: FragmentNoteViewBinding? = null
    private val binding: FragmentNoteViewBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteViewBinding equals null")

    private val component by lazy {
        (requireActivity().application as NotesApp).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)

        if (context is SendNoteTo) {
            sendNoteTo = context    // It is Intent.ACTION_SEND
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
        getNote()
        observeStateFragment()
        initOnScrollChangedListener()
        initActionBar()
        initBackPressed()
        initBottomAppBar()
        initFab()
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

    private fun initActionBar() {
        if (isNight) {
            binding.ivNoteViewCreate.setImageResource(R.drawable.ic_pencil_white_36dp)
        } else {
            binding.ivNoteViewCreate.setImageResource(R.drawable.ic_pencil_black_36dp)
        }
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
                copyContent()
            }
            ibNoteViewSendNote.setOnClickListener {
                sendNoteTo()
            }
            ibNoteViewDeleteNote.setOnClickListener {
                removeNote()
            }
        }
    }

    private fun copyContent() {
        val textToCopy = binding.tvNoteViewItselfNote.text

        val clipboardManager = requireActivity()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText(null, textToCopy)

        clipboardManager.setPrimaryClip(clip)

        showSnakebar(
            requireActivity().findViewById(R.id.main_container),
            binding.fabNoteViewExit,
            isNight,
            getString(R.string.copy_text_clipboard)
        )
    }

    private fun sendNoteTo() {
        val txtTitleNote = binding.tvNoteViewTitleNote.text.toString()

        val txtItselfNote = binding.tvNoteViewItselfNote.text.toString()

        sendNoteTo.sendNoteTO(txtTitleNote, txtItselfNote)
    }

    private fun removeNote() {
        val icon = if (isNight) R.drawable.ic_map_marker_question_outline_black_48dp
        else R.drawable.ic_map_marker_question_outline_white_48dp

        val snackBar = Snackbar.make(
            requireActivity().findViewById(R.id.main_container),
            resources.getString(R.string.confirm_remove_note),
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.move_note) {
                viewModel.removeNote(timeStamp)
            }
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
        snackBar.anchorView = binding.fabNoteViewExit
        snackBar.show()
    }

    private fun initFab() {
        binding.fabNoteViewExit.setOnClickListener {
            retryPreviousFragment()
        }
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    retryPreviousFragment()
                }

            }
        )
    }

    private fun getNote() {
        viewModel.getNote(timeStamp)
    }

    private fun initOnScrollChangedListener() {
        binding.svNoteView.doOnDetach {
            ViewTreeObserver.OnGlobalLayoutListener {
                binding.svNoteView.viewTreeObserver.removeOnScrollChangedListener {
                    binding.svNoteView.viewTreeObserver.addOnScrollChangedListener {
                        onScrollChangedListener()
                    }
                }
            }
        }
    }

    private fun onScrollChangedListener() {
        with(binding) {
            if (svNoteView.scrollY > 0) {
                babNoteView.performHide()
                fabNoteViewExit.hide()
            } else {
                babNoteView.performShow()
                fabNoteViewExit.show()
            }
        }
    }

    private fun observeStateFragment() {
        viewModel.stateNoteView.observe(viewLifecycleOwner) {
            when (it) {
                EndUsing -> {
                    retryPreviousFragment()
                }
                is NoteViewNote -> {
                    setNote(it.note)
                }
            }
        }
    }

    private fun setNote(note: Note) {
        val colorNote =
            if (isNight) NoteColors.noteColor[NoteColors.DARK_COLOR][note.colorIndex]
            else NoteColors.noteColor[NoteColors.LIGHT_COLOR][note.colorIndex]

        val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
        else R.drawable.ic_lock_black_36dp

        val imgUnlock = if (isNight) R.drawable.ic_lock_open_white_36dp
        else R.drawable.ic_lock_open_black_36dp

        with(binding) {
            cvNoteViewItselfNote.setBackgroundResource(colorNote)
            cvNoteViewTitle.setBackgroundResource(colorNote)
            ivNoteViewLock.setImageResource(imgLock)

            tvNoteViewFulldate.text = TimeUtils.getFullDate(note.timeStamp)
            tvNoteViewTitleNote.text = note.titleNote
            tvNoteViewItselfNote.text = note.itselfNote

            if (note.isLocked) {
                ivNoteViewLock.setImageResource(imgLock)
                tvNoteViewLock.text = requireActivity().resources.getString(R.string.lock_note)
            } else {
                ivNoteViewLock.setImageResource(imgUnlock)
                tvNoteViewLock.text = requireActivity().resources.getString(R.string.unlock_note)
            }

        }
    }

    private fun retryPreviousFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            null,
            0
        )
    }

    private fun launchNoteEntryEditFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
            .replace(R.id.main_container, NoteEditFragment.newInstanceEditMode(timeStamp))
            .addToBackStack(NoteEditFragment.NAME)
            .commit()
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
    }

    // It is Intent.ACTION_SEND
    interface SendNoteTo {
        fun sendNoteTO(txtTitleNote: String, txtItselfNote: String)
    }

    companion object {

        const val NAME = "note_view_fragment"
        private const val TIME_STAMP = "time_stamp"

        @JvmStatic
        fun newInstance(timeStamp: Long) =
            NoteViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(TIME_STAMP, timeStamp)
                }
            }
    }
}