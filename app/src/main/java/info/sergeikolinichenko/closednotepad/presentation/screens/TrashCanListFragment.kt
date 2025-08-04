package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentTrashCanListBinding
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.presentation.NotesApp
import info.sergeikolinichenko.closednotepad.presentation.adapters.trashcanlist.TrashCanAdapter
import info.sergeikolinichenko.closednotepad.presentation.utils.readinessCheckBiometric
import info.sergeikolinichenko.closednotepad.presentation.utils.showSnakebar
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNotesFactory
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelTrashCanList
import javax.inject.Inject

class TrashCanListFragment : Fragment() {

    private var _binding: FragmentTrashCanListBinding? = null
    private val binding: FragmentTrashCanListBinding
        get() = _binding ?: throw RuntimeException("FragmentTrashCanListBinding equals null")

    @Inject
    lateinit var viewModelFactory: ViewModelNotesFactory
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelTrashCanList::class.java]
    }

    private val adapterTrashCanList by lazy { TrashCanAdapter() }

    private var isNight = false
    private var wasHidden = false

    private var imgDelete = 0   // id ib_note_list_delete AppCompatImageView
    private var imgRecovery = 0   // id ib_note_list_delete AppCompatImageView

    private val component by lazy {
        (requireActivity().application as NotesApp).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

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
        adapterTrashCanList.onReNoteClick = {
            if (viewModel.isSelected.value == false || viewModel.isSelected.value == null) {
                if (it.isLocked) getBiometricSuccess(it.timeStamp, ::launchTrashCanViewFragment)
                else launchTrashCanViewFragment(it.timeStamp)
            } else {
                viewModel.selectRemovedNotes(it)
            }
        }
        adapterTrashCanList.onReNoteLongClick = {
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
            showSnakebar(
                requireActivity().findViewById(R.id.main_container),
                binding.fabTrashCanListExit,
                isNight,
                resources.getString(R.string.confirm_deletion_notes),
                R.string.delete_note,
                { viewModel.deleteRemovedNotes() },
                { viewModel.reselectRemovedNotes() }
            )
        }
        binding.ibTrashCanListDeleteOff.setOnClickListener {
            showSnakebar(
                requireActivity().findViewById(R.id.main_container),
                binding.fabTrashCanListExit,
                isNight,
                resources.getString(R.string.confirm_recovery_notes),
                R.string.recovery_note,
                { viewModel.recoveryRemovedNotes() },
                { viewModel.reselectRemovedNotes() }
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
                if (viewModel.isSelected.value == false || viewModel.isSelected.value == null) {
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

    private fun getBiometricSuccess(timeStamp: Long, act: (tm: Long) -> Unit) {
        val check = readinessCheckBiometric(
            this,
            BiometricManager.from(requireActivity().application),
            binding.fabTrashCanListExit,
            isNight,
            ::showSnakebar
        )
        if (check) {
            info.sergeikolinichenko.closednotepad.presentation.utils.authUser(
                this,
                ContextCompat.getMainExecutor(requireActivity().application),
                binding.fabTrashCanListExit,
                isNight,
                timeStamp,
                act,
                ::showSnakebar
            )
        }
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