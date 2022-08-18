package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteListBinding
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.adapters.notelist.NoteListAdapter
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.notelist.ViewModelNoteList
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.notelist.ViewModelNoteListFactory

class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    private val binding: FragmentNoteListBinding
        get() = _binding ?: throw RuntimeException("NoteListFragmentBinding equals null")

    private val viewModelFactory by lazy {
        ViewModelNoteListFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteList::class.java]
    }

    private val adapterNoteList by lazy { NoteListAdapter() }
    private var isNight = false

    private lateinit var finishApp: FinishApp
    private var isNotesSelected = false

    private var imgDelete = 0   // id ib_note_list_delete AppCompatImageView
    private var imgPallet = 0   // id ib_note_list_palette AppCompatImageView
    private var wasBabHiden = false // was BottomAppBar visible

    private var behaviorColorButtons = BottomSheetBehavior<ConstraintLayout>()
    private var behaviorBottomAppBar = HideBottomViewOnScrollBehavior<BottomAppBar>()
    private var behaviorOrderButtons = HideBottomViewOnScrollBehavior<ConstraintLayout>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FinishApp) {
            finishApp = context
        } else {
            throw RuntimeException(
                "There is no implementation of the interface FinishApp" +
                        " in the activity $context"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNoteListBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNoteList()
        observeIsSelected()
        isNightMode()
        initToolbar()
        initFabAddNote()
        initBottomAppBar()
        initRecyclerView()
        initNoteClickListeners()
        initColorButtons()
        initBackPressed()
        initOrderButtons()
        onRecyclerViewChangeListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initOrderButtons() {
        with(binding) {
            mbTimeStampSortAscending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TIME_STAMP_ASCENDING)
                hideOrderButtons()
            }
            mbTimeStampSortDescending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TITLE_DESCENDING)
                hideOrderButtons()
            }
            mbTitleAlphabetSortAscending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TITLE_ASCENDING)
                hideOrderButtons()
            }
            mbTitleAlphabetSortDescending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TITLE_DESCENDING)
                hideOrderButtons()
            }
            mbPaletteSort.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_PALETTE)
                hideOrderButtons()
            }
        }
    }

    private fun observeNoteList() {
        viewModel.noteList.observe(viewLifecycleOwner) {
            adapterNoteList.submitList(orderViewNoteList(it))

        }
    }

    private fun orderViewNoteList(list: List<Note>): List<Note> {
        setImageSortVariant()
        return when (viewModel.orderViewNoteList) {
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

    private fun initToolbar() {
        with(binding) {
            if (isNight) {
                ivOutlineNoteList.setImageResource(R.drawable.ic_exit_to_app_white_36dp)
            } else {
                ivOutlineNoteList.setImageResource(R.drawable.ic_exit_to_app_black_36dp)
            }
            setImageSortVariant()
            ivOutlineNoteList.setOnClickListener {
                finishApp.finishApp()
            }
            ivNoteListSortShown.setOnClickListener {
                val isClickable: Boolean = binding.clNoteListSort.isClickable
                if (!isClickable) {
                    showOrderButtons()
                } else {
                    hideOrderButtons()
                }
            }
        }
    }

    private fun setImageSortVariant() {
        val sortVariant = when (viewModel.orderViewNoteList) {
            SORT_TIME_STAMP_ASCENDING ->
                if (isNight) R.drawable.ic_sort_ascending_white_36dp
                else R.drawable.ic_sort_ascending_black_36dp

            SORT_TIME_STAMP_DESCENDING ->
                if (isNight) R.drawable.ic_sort_descending_white_36dp
                else R.drawable.ic_sort_descending_black_36dp

            SORT_TITLE_ASCENDING ->
                if (isNight) R.drawable.ic_sort_alphabetical_ascending_white_36dp
                else R.drawable.ic_sort_alphabetical_ascending_black_36dp

            SORT_TITLE_DESCENDING ->
                if (isNight) R.drawable.ic_sort_alphabetical_descending_white_36dp
                else R.drawable.ic_sort_alphabetical_descending_black_36dp

            else -> if (isNight) R.drawable.ic_filter_variant_white_36dp
            else R.drawable.ic_filter_variant_black_36dp
        }
        sortVariant.let {
            binding.ivNoteListSortShown.setImageResource(it)
        }
    }

    private fun initFabAddNote() {
        binding.fabAddNoteList.setOnClickListener {
            launchNoteEditFragment()
        }
    }

    private fun initBottomAppBar() {
        behaviorBottomAppBar = binding.babNoteList.behavior
        if (isNight) {
            binding.ibNoteListCog.setImageResource(R.drawable.ic_cog_white_36dp)
            binding.ibNoteListMagnify.setImageResource(R.drawable.ic_magnify_white_36dp)
            imgDelete = R.drawable.ic_delete_white_36dp
            imgPallet = R.drawable.ic_palette_white_36dp
        } else {
            binding.ibNoteListCog.setImageResource(R.drawable.ic_cog_black_36dp)
            binding.ibNoteListMagnify.setImageResource(R.drawable.ic_magnify_black_36dp)
            imgDelete = R.drawable.ic_delete_black_36dp
            imgPallet = R.drawable.ic_palette_black_36dp
        }
        binding.ibNoteListCog.setOnClickListener {
            launchSettingsFragment()
        }
        binding.ibNoteListPalette.setOnClickListener {
            if (isNotesSelected) {
                showColorButtons()
            }
        }
        binding.ibNoteListDelete.setOnClickListener {
            removeNotes()
        }
        binding.ibNoteListMagnify.setOnClickListener {
            launchNoteSearchFragment()
        }
    }

    private fun showColorButtons() {
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_HIDDEN) {
            behaviorColorButtons.state = BottomSheetBehavior.STATE_EXPANDED
        }
//        binding.fabAddNoteList.hide()
//        behaviorBottomAppBar.slideDown(binding.babNoteList)
    }

    private fun hideColorButtons() {
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_EXPANDED) {
            behaviorColorButtons.state = BottomSheetBehavior.STATE_HIDDEN
        }
//        behaviorBottomAppBar.slideUp(binding.babNoteList)
//        binding.fabAddNoteList.show()
    }

    // Delete selected items from collections
    private fun removeNotes() {
        if (isNotesSelected) {
            val snackBar = Snackbar.make(
                requireActivity().findViewById(R.id.main_container),
                resources.getString(R.string.confirm_deletion_notes),
                Snackbar.LENGTH_LONG
            )
                .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        viewModel.resetSelectedNotes()
                    }
                })
                .setAction(R.string.delete_note) {
                    viewModel.removeNotes()
                }
            snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
            snackBar.anchorView = binding.fabAddNoteList
            snackBar.show()
        }
    }

    private fun initRecyclerView() {
        val noteListRecyclerView: RecyclerView = binding.recyclerView
        noteListRecyclerView.adapter = adapterNoteList
    }

    private fun initNoteClickListeners() {
        adapterNoteList.onNoteClick = {
            if (!isNotesSelected) {
                // Go to NoteViewFragment
                launchNoteViewFragment(it.timeStamp)
                if (behaviorBottomAppBar.isScrolledDown) {
                    showFabAndBab()
                }
            } else {
                // Deselecting elements collections
                viewModel.selectNotesAtNote(it)
            }
        }
        adapterNoteList.onNoteLongClick = {
            // Selecting elements collections
            viewModel.selectNotesAtNote(it)
        }
    }

    private fun observeIsSelected() {
        // Get an indicator whether there are selected elements collections
        viewModel.isSelected.observe(viewLifecycleOwner) {
            isNotesSelected = it
            if (it) {
                with(binding) {
                    ibNoteListDelete.setImageResource(imgDelete)
                    ibNoteListPalette.setImageResource(imgPallet)
                    ibNoteListDelete.isClickable = true
                    ibNoteListPalette.isClickable = true
                }
                if (behaviorBottomAppBar.isScrolledDown) {
                    wasBabHiden = true
                    showFabAndBab()
                }
            } else {
                with(binding) {
                    ibNoteListDelete.setImageResource(R.drawable.ic_delete_grey600_36dp)
                    ibNoteListPalette.setImageResource(R.drawable.ic_palette_grey600_36dp)
                    ibNoteListDelete.isClickable = false
                    ibNoteListPalette.isClickable = false
                }
                if (behaviorBottomAppBar.isScrolledUp && wasBabHiden) {
                    hideFabAndBab()
                    wasBabHiden = false
                }
            }
        }
    }

    private fun showOrderButtons() {
        val animShowOrderButtons = AnimationUtils.loadAnimation(context, R.anim.order_buttons_show)
        with(binding) {
            val lLayoutParams = clNoteListSort.layoutParams as CoordinatorLayout.LayoutParams
            lLayoutParams.marginEnd += (clNoteListSort.width * 1.15).toInt()
            clNoteListSort.layoutParams = lLayoutParams
            clNoteListSort.startAnimation(animShowOrderButtons)
            clNoteListSort.isClickable = true
        }
    }

    private fun hideOrderButtons() {
        val animHideOrderButtons = AnimationUtils.loadAnimation(context, R.anim.order_buttons_hide)
        with(binding) {
            val lLayoutParams = clNoteListSort.layoutParams as CoordinatorLayout.LayoutParams
            lLayoutParams.marginEnd -= (clNoteListSort.width * 1.15).toInt()
            clNoteListSort.layoutParams = lLayoutParams
            clNoteListSort.startAnimation(animHideOrderButtons)
            clNoteListSort.isClickable = false
        }
    }

    private fun showFabAndBab() {
        binding.fabAddNoteList.show()
        binding.babNoteList.performShow(true)
    }

    private fun hideFabAndBab() {
        binding.babNoteList.performHide(true)
        binding.fabAddNoteList.hide()
    }

    private fun initColorButtons() {
        behaviorColorButtons = BottomSheetBehavior.from(
            requireActivity().findViewById(R.id.cl_note_list_color_buttons)
        )
        behaviorColorButtons.state = BottomSheetBehavior.STATE_HIDDEN

        val shadeColor = if (isNight) NoteColors.DARK_COLOR
        else NoteColors.LIGHT_COLOR
        with(binding) {
            mbNoteListColorPink.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.PINK])
            )
            mbNoteListColorPurple.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.PURPLE])
            )
            mbNoteListColorIndigo.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.INDIGO])
            )
            mbNoteListColorGreen.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.GREEN])
            )
            mbNoteListColorOrange.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.ORANGE])
            )
            mbNoteListColorBrown.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.BROWN])
            )
            mbNoteListColorGray.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.GRAY])
            )
            mbNoteListColorBlueGray.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.noteColor[shadeColor][NoteColors.BLUE_GRAY])
            )

            mbNoteListColorPink.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PINK)
                hideColorButtons()
            }
            mbNoteListColorPink.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PINK)
                hideColorButtons()
            }
            mbNoteListColorPurple.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PURPLE)
                hideColorButtons()
            }
            mbNoteListColorIndigo.setOnClickListener {
                viewModel.setColorIndex(NoteColors.INDIGO)
                hideColorButtons()
            }
            mbNoteListColorGreen.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GREEN)
                hideColorButtons()
            }
            mbNoteListColorOrange.setOnClickListener {
                viewModel.setColorIndex(NoteColors.ORANGE)
                hideColorButtons()
            }
            mbNoteListColorBrown.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BROWN)
                hideColorButtons()
            }
            mbNoteListColorGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GRAY)
                hideColorButtons()
            }
            mbNoteListColorBlueGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BLUE_GRAY)
                hideColorButtons()
            }
        }
    }

    private fun onRecyclerViewChangeListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isNotesSelected) {
                    if (dy > 0) {
                        if (behaviorBottomAppBar.isScrolledUp) {
                            hideFabAndBab()
                        }
                    } else {
                        if (behaviorBottomAppBar.isScrolledDown) {
                            showFabAndBab()
                        }
                    }
                }
            }
        })
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishNoteListFragment()
                }

            }
        )
    }

    private fun launchNoteViewFragment(timeStamp: Long) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
            .replace(R.id.main_container, NoteViewFragment.newInstance(timeStamp))
            .addToBackStack(NoteViewFragment.NAME)
            .commit()
    }

    private fun launchNoteEditFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_left, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_left
            )
            .replace(R.id.main_container, NoteEditFragment.newInstanceAddMode())
            .addToBackStack(NoteEditFragment.NAME)
            .commit()
    }

    private fun launchNoteSearchFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_top,
                R.anim.exit_to_bottom,
                R.anim.enter_from_bottom,
                R.anim.exit_to_top
            )
            .replace(R.id.main_container, NoteSearchFragment.newInstance())
            .addToBackStack(NoteSearchFragment.NAME)
            .commit()
    }

    private fun launchSettingsFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_bottom,
                R.anim.exit_to_top,
                R.anim.enter_from_top,
                R.anim.exit_to_bottom
            )
            .replace(R.id.main_container, SettingsFragment.newInstance())
            .addToBackStack(SettingsFragment.NAME)
            .commit()
    }

    // needed to close the application on the button back
    private fun finishNoteListFragment() {
        requireActivity().finish()
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
        adapterNoteList.isNight = isNight
    }

    // needed to close the application
    interface FinishApp {
        fun finishApp()
    }

    companion object {
        const val NAME = "note_list_fragment"
        const val SORT_TIME_STAMP_ASCENDING = "sort_time_stamp_ascending"
        private const val SORT_TIME_STAMP_DESCENDING = "sort_time_stamp_descending"
        private const val SORT_TITLE_ASCENDING = "sort_title_ascending"
        private const val SORT_TITLE_DESCENDING = "sort_title_descending"
        private const val SORT_PALETTE = "sort_palette"

        @JvmStatic
        fun newInstance() = NoteListFragment()
    }

}