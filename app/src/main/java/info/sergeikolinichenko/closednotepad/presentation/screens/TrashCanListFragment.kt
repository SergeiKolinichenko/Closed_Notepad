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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentTrashCanListBinding
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.presentation.adapters.trashcanlist.TrashCanAdapter
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanlist.ViewModelTrashCanList
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanlist.ViewModelTrashCanListFactory

class TrashCanListFragment : Fragment() {

    private var _binding: FragmentTrashCanListBinding? = null
    private val binding: FragmentTrashCanListBinding
        get() = _binding ?: throw RuntimeException("FragmentTrashCanListBinding equals null")

    private val viewModelFactory by lazy {
        ViewModelTrashCanListFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelTrashCanList::class.java]
    }

    private val adapterTrashCanList by lazy { TrashCanAdapter() }

    private var isNight = false
    private var isRemovedNoteSelected = false
    private var wasHidden = false

    private var imgDelete = 0   // id ib_note_list_delete AppCompatImageView
    private var imgRecovery = 0   // id ib_note_list_delete AppCompatImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashCanListBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onRecyclerViewScrollListener()
        observeTrashcanList()
        observeIsSelected()
        isNightMode()
        initRecyclerView()
        initTrashCanClickListeners()
        initBottomAppBar()
        initFabExit()
        initBackPressed()
    }

    private fun observeTrashcanList() {
        viewModel.removedNoteList.observe(viewLifecycleOwner) {
            adapterTrashCanList.submitList(orderViewTrashcanList(it))
        }
    }

    private fun observeIsSelected() {
        viewModel.isSelected.observe(viewLifecycleOwner) {
            isRemovedNoteSelected = it
            with(binding) {
                if (it) {
                    if (babTrashCanList.isScrolledDown) {
                        wasHidden = true
                        showFabAndBab()
                    }
                    ibTrashCanListDelete.setImageResource(imgDelete)
                    ibTrashCanListDeleteOff.setImageResource(imgRecovery)
                    ibTrashCanListDelete.isClickable = true
                    ibTrashCanListDeleteOff.isClickable = true
                } else {
                    if (wasHidden && babTrashCanList.isScrolledUp) {
                        hideFabAndBab()
                        wasHidden = false
                    }
                    ibTrashCanListDelete.setImageResource(
                        R.drawable.ic_delete_grey600_36dp
                    )
                    ibTrashCanListDeleteOff.setImageResource(
                        R.drawable.ic_delete_off_grey600_36dp
                    )
                    ibTrashCanListDelete.isClickable = false
                    ibTrashCanListDeleteOff.isClickable = false
                }
            }
        }
    }

    private fun orderViewTrashcanList(list: List<RemovedNote>): List<RemovedNote> {
        return when (viewModel.orderViewTrashCanList) {
            SORT_TIME_STAMP_ASCENDING -> list.sortedBy { it.timeStamp }
            SORT_TIME_STAMP_DESCENDING -> list.sortedByDescending { it.timeStamp }
            SORT_TITLE_ASCENDING -> list.sortedBy { it.titleNote }
            SORT_TITLE_DESCENDING -> list.sortedByDescending { it.titleNote }
            SORT_PALETTE -> list.sortedBy { it.colorIndex }
            else -> {
                return list.sortedBy { it.timeStamp }
            }
        }
    }

    private fun initRecyclerView() {
        val noteListRecyclerView: RecyclerView = binding.recyclerViewTrashCan
        noteListRecyclerView.adapter = adapterTrashCanList
    }

    private fun initTrashCanClickListeners() {
        adapterTrashCanList.onNoteClick = {
            if (!isRemovedNoteSelected) {
                launchTrashCanViewFragment(it.timeStamp)
            } else {
                viewModel.selectRemovedNotes(it)
            }
        }
        adapterTrashCanList.onNoteLongClick = {
            viewModel.selectRemovedNotes(it)
        }
    }

    private fun initFabExit() {
        binding.fabTrashCanListExit.setOnClickListener {
            retryNoteListFragment()
        }
    }

    private fun initBottomAppBar() {
        if (isNight) {
            imgDelete = R.drawable.ic_delete_white_36dp
            imgRecovery = R.drawable.ic_delete_off_white_36dp
        } else {
            imgDelete = R.drawable.ic_delete_black_36dp
            imgRecovery = R.drawable.ic_delete_off_black_36dp
        }
        binding.ibTrashCanListDelete.setOnClickListener {
            showSnackbar(
                R.string.confirm_deletion_notes,
                R.string.delete_note,
                { viewModel.reselectRemovedNotes() },
                { viewModel.deleteRemovedNotes() }
            )
        }
        binding.ibTrashCanListDeleteOff.setOnClickListener {
            showSnackbar(
                R.string.confirm_recovery_notes,
                R.string.recovery_note,
                { viewModel.reselectRemovedNotes() },
                { viewModel.recoveryRemovedNotes() }
            )
        }
        binding.ibTrashCanListDelete.isClickable = false
        binding.ibTrashCanListDeleteOff.isClickable = false
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

    private fun onRecyclerViewScrollListener() {
        binding.recyclerViewTrashCan.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isRemovedNoteSelected) {
                    if (dy > 0) {
                        if (binding.babTrashCanList.isScrolledUp) {
                            hideFabAndBab()
                        }
                    } else {
                        if (binding.babTrashCanList.isScrolledDown) {
                            showFabAndBab()
                        }
                    }
                }
            }
        })
    }

    private fun hideFabAndBab() {
        binding.babTrashCanList.performHide(true)
        binding.fabTrashCanListExit.hide()
    }

    private fun showFabAndBab() {
        binding.fabTrashCanListExit.show()
        binding.babTrashCanList.performShow(true)
    }

    private fun launchTrashCanViewFragment(timeStamp: Long) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
            .replace(R.id.main_container, TrashCanViewFragment.newInstance(timeStamp))
            .addToBackStack(NoteEditFragment.NAME)
            .commit()
    }

    private fun showSnackbar(
        message: Int,
        textButton: Int,
        cancel: () -> Unit,
        action: () -> Unit
    ) {
        val icon = if (isNight) R.drawable.ic_map_marker_question_outline_black_48dp
        else R.drawable.ic_map_marker_question_outline_white_48dp

        val snackBar = Snackbar.make(
            requireActivity().findViewById(R.id.main_container),
            resources.getString(message),
            Snackbar.LENGTH_LONG
        )
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    cancel()
                }
            })
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
        snackBar.anchorView = binding.fabTrashCanListExit
        snackBar.show()
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
        adapterTrashCanList.isNight = isNight
    }

    private fun retryNoteListFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0
        )
    }

    companion object {
        const val NAME = "trash_can_list_fragment"

        private const val SORT_TIME_STAMP_ASCENDING = "sort_time_stamp_ascending"
        private const val SORT_TIME_STAMP_DESCENDING = "sort_time_stamp_descending"
        private const val SORT_TITLE_ASCENDING = "sort_title_ascending"
        private const val SORT_TITLE_DESCENDING = "sort_title_descending"
        private const val SORT_PALETTE = "sort_palette"

        @JvmStatic
        fun newInstance() = TrashCanListFragment()
    }
}