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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteListBinding
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.adapters.notelist.NoteListAdapter
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteList
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteListFactory

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
        observeBottomAppBar()
        observeNoteList()
        isNightMode()
        initToolbar()
        initFabAddNote()
        initBottomAppBar()
        initRecyclerView()
        initNoteClickListeners()
        initColorButtons()
        initBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeNoteList() {
        viewModel.noteList.observe(viewLifecycleOwner) {
            adapterNoteList.submitList(sortListNotes(it))
        }
    }

    private fun sortListNotes(list: List<Note>): List<Note> {
        return list.sortedBy { it.timeStamp }
    }

    private fun initToolbar() {
        if (isNight){
            binding.ibNoteListFilter.setImageResource(R.drawable.ic_filter_variant_white_36dp)
        } else {
            binding.ibNoteListFilter.setImageResource(R.drawable.ic_filter_variant_black_36dp)
        }
        binding.ivOutlineNoteList.setOnClickListener {
            finishApp.finishApp()
        }
    }

    private fun initFabAddNote() {
        binding.fabAddNoteList.setOnClickListener {
            launchNoteEditFragment()
        }
    }

    private fun initBottomAppBar(){
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
            TODO()
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
            TODO()
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
            } else {
                with(binding) {
                    ibNoteListDelete.setImageResource(R.drawable.ic_delete_grey600_36dp)
                    ibNoteListPalette.setImageResource(R.drawable.ic_palette_grey600_36dp)
                    ibNoteListDelete.isClickable = false
                    ibNoteListPalette.isClickable = false
                }
            }
        }
        adapterNoteList.onNoteClick = {
            if (!isNotesSelected) {
                // Go to NoteViewFragment
                launchNoteViewFragment(it)
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

    private fun initColorButtons() {
        val shadeColor = if (isNight) NoteColors.DARK_COLOR
        else NoteColors.LIGHT_COLOR
        with(binding) {
            mbNoteListColorPink.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.PINK]))
            mbNoteListColorPurple.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.PURPLE]))
            mbNoteListColorIndigo.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.INDIGO]))
            mbNoteListColorGreen.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.GREEN]))
            mbNoteListColorOrange.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.ORANGE]))
            mbNoteListColorBrown.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.BROWN]))
            mbNoteListColorGray.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.GRAY]))
            mbNoteListColorBlueGray.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(NoteColors.entriesColor[shadeColor][NoteColors.BLUE_GRAY]))

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

    private fun observeBottomAppBar() {
//        viewModel.removeNoteEntries.observe(viewLifecycleOwner) {
//            removeEntriesFromNote()
//            Log.d("MyLog", "1111")
//        }
//        viewModel.paletteNoteEntries.observe(viewLifecycleOwner) {
//            showFab()
//            Log.d("MyLog", "2222")
//        }
    }

    private fun launchNoteViewFragment(noteEntry: Note) {
        val timeStamp = noteEntry.timeStamp
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
        val mb = binding.mbNoteListColorPink
        showColorButtons(mb)
    }

    private fun hidePinkButton() {
        val mb = binding.mbNoteListColorPink
        hideColorButtons(mb)
    }

    private fun showPurpleButton() {
        val mb = binding.mbNoteListColorPurple
        showColorButtons(mb)
    }

    private fun hidePurpleButton() {
        val mb = binding.mbNoteListColorPurple
        hideColorButtons(mb)
    }

    private fun showIndigoButton() {
        val mb = binding.mbNoteListColorIndigo
        showColorButtons(mb)
    }

    private fun hideIndigoButton() {
        val mb = binding.mbNoteListColorIndigo
        hideColorButtons(mb)
    }

    private fun showGreenButton() {
        val mb = binding.mbNoteListColorGreen
        showColorButtons(mb)
    }

    private fun hideGreenButton() {
        val mb = binding.mbNoteListColorGreen
        hideColorButtons(mb)
    }

    private fun showOrangeButton() {
        val mb = binding.mbNoteListColorOrange
        showColorButtons(mb)
    }

    private fun hideOrangeButton() {
        val mb = binding.mbNoteListColorOrange
        hideColorButtons(mb)
    }

    private fun showBrownButton() {
        val mb = binding.mbNoteListColorBrown
        showColorButtons(mb)
    }

    private fun hideBrownButton() {
        val mb = binding.mbNoteListColorBrown
        hideColorButtons(mb)
    }

    private fun showGrayButton() {
        val mb = binding.mbNoteListColorGray
        showColorButtons(mb)
    }

    private fun hideGrayButton() {
        val mb = binding.mbNoteListColorGray
        hideColorButtons(mb)
    }

    private fun showBlueGrayButton() {
        val mb = binding.mbNoteListColorBlueGray
        showColorButtons(mb)
    }

    private fun hideBlueGrayButton() {
        val mb = binding.mbNoteListColorBlueGray
        hideColorButtons(mb)
    }

    private fun showColorButtons(mb: MaterialButton?) {
        val showButton = AnimationUtils.loadAnimation(requireContext(), R.anim.show_color_button)
        val layoutParams = mb?.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.marginEnd += (mb.width * 1.05).toInt()
        mb.layoutParams = layoutParams
        mb.startAnimation(showButton)
        mb.visibility = View.VISIBLE
        mb.isClickable = true
    }

    private fun hideColorButtons(mb: MaterialButton?) {
        val hideButton = AnimationUtils.loadAnimation(requireContext(), R.anim.hide_color_button)
        val layoutParams = mb?.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.marginEnd -= (mb.width * 1.05).toInt()
        mb.layoutParams = layoutParams
        mb.startAnimation(hideButton)
        mb.visibility = View.INVISIBLE
        mb.isClickable = false
    }

    companion object {
        const val NAME = "note_list_fragment"

        @JvmStatic
        fun newInstance() = NoteListFragment()
    }

}