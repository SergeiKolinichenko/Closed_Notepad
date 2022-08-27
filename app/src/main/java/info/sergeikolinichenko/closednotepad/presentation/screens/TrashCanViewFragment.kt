package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentTrashCanViewBinding
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanview.ViewModelTrashCanView
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanview.ViewModelTrashCanViewFactory

class TrashCanViewFragment : Fragment() {

    private var _binding: FragmentTrashCanViewBinding? = null
    private val binding: FragmentTrashCanViewBinding
        get() = _binding ?: throw RuntimeException("FragmentTrashCanViewBinding equals null")

    private val viewModelFactory by lazy {
        ViewModelTrashCanViewFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelTrashCanView::class.java]
    }

    private var _timeStamp: Long? = null
    private val timeStamp: Long
        get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var isNight = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
        isNightMode()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashCanViewBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeRemovedNote()
        observeEndUsingFragment()
        onScrollViewChangeListener()
        initBackPressed()
        initActionBar()
        initBottomAppBar()
        initFabExit()
    }

    private fun observeRemovedNote() {
        viewModel.removedNote.observe(viewLifecycleOwner) {
            val colorNote =
                if (isNight) NoteColors.noteColor[NoteColors.DARK_COLOR][it.colorIndex]
                else NoteColors.noteColor[NoteColors.LIGHT_COLOR][it.colorIndex]

            val imgLock = if (isNight) R.drawable.ic_lock_white_24dp
            else R.drawable.ic_lock_black_24dp

            with(binding) {
                cvTrashCanViewTitle.setBackgroundResource(colorNote)
                cvTrashCanViewItself.setBackgroundResource(colorNote)
                ivTrashCanViewLock.setImageResource(imgLock)

                tvTrashCanViewDateCreate.text = TimeUtils.getDate(it.timeStamp)
                tvTrashCanViewDateRemove.text = TimeUtils.getDate(it.timeRemove)
                tvTrashCanViewTitle.text = it.titleNote
                tvTrashCanViewItself.text = it.itselfNote

                ivTrashCanViewLock.visibility = if (it.isLocked) View.VISIBLE
                else View.INVISIBLE
            }
        }
        viewModel.getRemovedNote(timeStamp)
    }

    private fun observeEndUsingFragment() {
        viewModel.endUsingFragment.observe(viewLifecycleOwner) {
            retryTrashCanListFragment()
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(TIME_STAMP)) {
            throw RuntimeException("Arguments don't contains time_stamp")
        }
        _timeStamp = requireArguments().getLong(TIME_STAMP)
    }

    private fun initActionBar() {
        val imgPencil = if (isNight) R.drawable.ic_pencil_white_24dp
        else R.drawable.ic_pencil_black_24dp

        val imgRemove = if (isNight) R.drawable.ic_delete_white_24dp
        else R.drawable.ic_delete_black_24dp

        with(binding){
            ivTrashCanViewPencil.setImageResource(imgPencil)
            ivTrashCanViewRemove.setImageResource(imgRemove)
        }
    }

    private fun initBottomAppBar() {
        with(binding) {
            if (isNight) {
                ibTrashCanViewDelete.setImageResource(R.drawable.ic_delete_white_36dp)
                ibTrashCanViewDeleteOff.setImageResource(R.drawable.ic_delete_off_white_36dp)
            } else {
                ibTrashCanViewDelete.setImageResource(R.drawable.ic_delete_black_36dp)
                ibTrashCanViewDeleteOff.setImageResource(R.drawable.ic_delete_off_black_36dp)
            }
            ibTrashCanViewDeleteOff.setOnClickListener {
                showSnackbar(
                    R.string.confirm_recovery_note,
                    R.string.recovery_note
                ) { viewModel.recoveryRemovedNote() }
            }
            ibTrashCanViewDelete.setOnClickListener {
                showSnackbar(
                    R.string.confirm_deletion_note,
                    R.string.delete_note
                ) { viewModel.deleteRemovedNote() }
            }
        }
    }

    private fun initFabExit() {
        binding.fabTrashCanViewExit.setOnClickListener {
            viewModel.endUsingFragment()
        }
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.endUsingFragment()
                }

            }
        )
    }

    private fun onScrollViewChangeListener(){
        binding.svTrashCanView.viewTreeObserver.addOnScrollChangedListener {
            with(binding) {
                if (svTrashCanView.scrollY > 0) {
                    babTrashCanView.performHide(true)
                    fabTrashCanViewExit.hide()
                } else {
                    fabTrashCanViewExit.show()
                    babTrashCanView.performShow(true)
                }
            }
        }
    }

    private fun showSnackbar(
        message: Int,
        textButton: Int,
        action: () -> Unit
    ) {
        val icon = if (isNight) R.drawable.ic_map_marker_question_outline_black_48dp
        else R.drawable.ic_map_marker_question_outline_white_48dp

        val snackBar = Snackbar.make(
            requireActivity().findViewById(R.id.main_container),
            resources.getString(message),
            Snackbar.LENGTH_LONG
        )
            .setAction(textButton) {
                action()
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
        snackBar.anchorView = binding.fabTrashCanViewExit
        snackBar.show()
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
    }

    private fun retryTrashCanListFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            null,
            0
        )
    }

    companion object {
        private const val TIME_STAMP = "time_stamp_trash_can"

        fun newInstance(timeStamp: Long) =
            TrashCanViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(TIME_STAMP, timeStamp)
                }
            }
    }
}