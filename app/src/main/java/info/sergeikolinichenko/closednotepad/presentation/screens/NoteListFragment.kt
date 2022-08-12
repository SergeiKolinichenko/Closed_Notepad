package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

    private val viewModelFactory by lazy {
        ViewModelNoteListFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteList::class.java]
    }

    private val adapterNoteList by lazy { NoteListAdapter() }
    private var isNight = false

    private var _binding: FragmentNoteListBinding? = null
    private val binding: FragmentNoteListBinding
        get() = _binding ?: throw RuntimeException("NoteListFragmentBinding equals null")

    private lateinit var finishApp: FinishApp
    private var isNotesSelected = false

    private var imgDelete = 0   // id ib_note_list_delete AppCompatImageView
    private var imgPallet = 0   // id ib_note_list_palette AppCompatImageView
    private var wasBabHiden = false // was BottomAppBar visible

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initOrderButtons() {
        with(binding) {
            fabTimeStampSortAscending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TIME_STAMP_ASCENDING)
                hideOrderButtons()
            }
            fabTimeStampSortDescending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TITLE_DESCENDING)
                hideOrderButtons()
            }
            fabTitleAlphabetSortAscending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TITLE_ASCENDING)
                hideOrderButtons()
            }
            fabTitleAlphabetSortDescending.setOnClickListener {
                viewModel.setOrderViewNoteList(SORT_TITLE_DESCENDING)
                hideOrderButtons()
            }
            fabPaletteSort.setOnClickListener {
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
                ivNoteListSortShown.setImageResource(R.drawable.ic_filter_variant_white_36dp)
            } else {
                ivOutlineNoteList.setImageResource(R.drawable.ic_exit_to_app_black_36dp)
                ivNoteListSortShown.setImageResource(R.drawable.ic_filter_variant_black_36dp)
            }

            ivOutlineNoteList.setOnClickListener {
                finishApp.finishApp()
            }
            ivNoteListSortShown.setOnClickListener {
                showOrderButtons()
            }
        }
    }

    private fun initFabAddNote() {
        binding.fabAddNoteList.setOnClickListener {
            launchNoteEditFragment()
        }
    }

    private fun initBottomAppBar() {
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
            binding.fabAddNoteList.show()
            binding.babNoteList.performShow()
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
        showPinkButton()
        showPurpleButton()
        showIndigoButton()
        showGreenButton()
        showOrangeButton()
        showBrownButton()
        showGrayButton()
        showBlueGrayButton()
    }

    private fun hideColorButtons() {
        hidePinkButton()
        hidePurpleButton()
        hideIndigoButton()
        hideGreenButton()
        hideOrangeButton()
        hideBrownButton()
        hideGrayButton()
        hideBlueGrayButton()
    }

    // Delete selected items from collections
    private fun removeNotes() {
        if (isNotesSelected) {
            Snackbar.make(
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
                .show()
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
                showFabAndBab()
            } else {
                with(binding) {
                    ibNoteListDelete.setImageResource(R.drawable.ic_delete_grey600_36dp)
                    ibNoteListPalette.setImageResource(R.drawable.ic_palette_grey600_36dp)
                    ibNoteListDelete.isClickable = false
                    ibNoteListPalette.isClickable = false
                }
                hideFabAndBab()
            }
        }
    }

    private fun getListOrderButtons(): List<FloatingActionButton> {
        val list = mutableListOf<FloatingActionButton?>()
        with(binding) {
            list.add(fabTimeStampSortAscending)
            list.add(fabTimeStampSortDescending)
            list.add(fabTitleAlphabetSortAscending)
            list.add(fabTitleAlphabetSortDescending)
            list.add(fabPaletteSort)
        }
        return list.filterNotNull()
    }

    private fun showOrderButtons() {
        val orient = requireActivity().resources.configuration.orientation
        val list = getListOrderButtons()
        var transY: Float
        var transX: Float
        var plusX = 0F
        var plusY = 0F
        if (orient == Configuration.ORIENTATION_PORTRAIT) {
            transY = 250F
            transX = -100F
            plusY = transY
        } else {
            transY = 200F
            transX = -100F
            plusX = -200F
        }
        for (item in list) {
            item.visibility = View.VISIBLE
            item.isClickable = true
            item.animate()
                .translationY(transY)
                .translationX(transX)
                .setDuration(500)
                .scaleX(1F)
                .scaleY(1F)
                .alpha(1F)
                .setInterpolator(LinearInterpolator())
                .start()
            transY += plusY
            transX += plusX
        }
    }

    private fun hideOrderButtons() {
        val list = getListOrderButtons()
        for (item in list) {
            item.isClickable = false
            item.animate()
                .translationY(0F)
                .translationX(0F)
                .setDuration(500)
                .scaleX(0F)
                .scaleY(0F)
                .alpha(0F)
                .setInterpolator(LinearInterpolator())
                .start()
        }
    }

    private fun showFabAndBab() {
        with(binding) {
            wasBabHiden = babNoteList.isScrolledDown
            if (wasBabHiden) {
                fabAddNoteList.animate().translationY(0F)
                    .scaleX(1F).scaleY(1F)
                    .setInterpolator(LinearInterpolator()).start()
                babNoteList.performShow()
            }
        }
    }

    private fun hideFabAndBab() {
        if (wasBabHiden) {
            with(binding) {
                val layoutParams =
                    fabAddNoteList.layoutParams as CoordinatorLayout.LayoutParams
                val fabBottomMargin = layoutParams.bottomMargin
                fabAddNoteList.animate()
                    .translationY((fabAddNoteList.height + fabBottomMargin).toFloat())
                    .scaleX(0.1F).scaleY(0.1F)
                    .setInterpolator(LinearInterpolator()).start()
                babNoteList.performHide()
            }
            wasBabHiden = false
        }
    }

    private fun initColorButtons() {
        val shadeColor = if (isNight) NoteColors.DARK_COLOR
        else NoteColors.LIGHT_COLOR
        with(binding) {
            fabNoteListColorPink.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.PINK])
            )
            fabNoteListColorPurple.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.PURPLE])
            )
            fabNoteListColorIndigo.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.INDIGO])
            )
            fabNoteListColorGreen.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.GREEN])
            )
            fabNoteListColorOrange.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.ORANGE])
            )
            fabNoteListColorBrown.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.BROWN])
            )
            fabNoteListColorGray.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.GRAY])
            )
            fabNoteListColorBlueGray.backgroundTintList = ColorStateList.valueOf(
                requireContext()
                    .getColor(NoteColors.entriesColor[shadeColor][NoteColors.BLUE_GRAY])
            )

            fabNoteListColorPink.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PINK)
                hideColorButtons()
            }
            fabNoteListColorPink.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PINK)
                hideColorButtons()
            }
            fabNoteListColorPurple.setOnClickListener {
                viewModel.setColorIndex(NoteColors.PURPLE)
                hideColorButtons()
            }
            fabNoteListColorIndigo.setOnClickListener {
                viewModel.setColorIndex(NoteColors.INDIGO)
                hideColorButtons()
            }
            fabNoteListColorGreen.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GREEN)
                hideColorButtons()
            }
            fabNoteListColorOrange.setOnClickListener {
                viewModel.setColorIndex(NoteColors.ORANGE)
                hideColorButtons()
            }
            fabNoteListColorBrown.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BROWN)
                hideColorButtons()
            }
            fabNoteListColorGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.GRAY)
                hideColorButtons()
            }
            fabNoteListColorBlueGray.setOnClickListener {
                viewModel.setColorIndex(NoteColors.BLUE_GRAY)
                hideColorButtons()
            }
        }
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

    private fun showPinkButton() {
        val mb = binding.fabNoteListColorPink
        showColorButtons(mb)
    }

    private fun hidePinkButton() {
        val mb = binding.fabNoteListColorPink
        hideColorButtons(mb)
    }

    private fun showPurpleButton() {
        val mb = binding.fabNoteListColorPurple
        showColorButtons(mb)
    }

    private fun hidePurpleButton() {
        val mb = binding.fabNoteListColorPurple
        hideColorButtons(mb)
    }

    private fun showIndigoButton() {
        val mb = binding.fabNoteListColorIndigo
        showColorButtons(mb)
    }

    private fun hideIndigoButton() {
        val mb = binding.fabNoteListColorIndigo
        hideColorButtons(mb)
    }

    private fun showGreenButton() {
        val mb = binding.fabNoteListColorGreen
        showColorButtons(mb)
    }

    private fun hideGreenButton() {
        val mb = binding.fabNoteListColorGreen
        hideColorButtons(mb)
    }

    private fun showOrangeButton() {
        val mb = binding.fabNoteListColorOrange
        showColorButtons(mb)
    }

    private fun hideOrangeButton() {
        val mb = binding.fabNoteListColorOrange
        hideColorButtons(mb)
    }

    private fun showBrownButton() {
        val mb = binding.fabNoteListColorBrown
        showColorButtons(mb)
    }

    private fun hideBrownButton() {
        val mb = binding.fabNoteListColorBrown
        hideColorButtons(mb)
    }

    private fun showGrayButton() {
        val mb = binding.fabNoteListColorGray
        showColorButtons(mb)
    }

    private fun hideGrayButton() {
        val mb = binding.fabNoteListColorGray
        hideColorButtons(mb)
    }

    private fun showBlueGrayButton() {
        val mb = binding.fabNoteListColorBlueGray
        showColorButtons(mb)
    }

    private fun hideBlueGrayButton() {
        val mb = binding.fabNoteListColorBlueGray
        hideColorButtons(mb)
    }

    private fun showColorButtons(view: View) {
        view.animate().alpha(1F).scaleX(1F).scaleY(1F).setDuration(500).start()
        view.visibility = View.VISIBLE
        view.isClickable = true
    }

    private fun hideColorButtons(view: View) {
        view.animate().alpha(0F).scaleX(0F).scaleY(0F).setDuration(500).start()
        view.isClickable = false
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