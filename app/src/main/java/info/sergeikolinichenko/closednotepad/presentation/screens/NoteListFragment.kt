package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
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
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.presentation.adapters.notelist.NoteListAdapter
import info.sergeikolinichenko.closednotepad.presentation.utils.EntriesColors
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
        get() = _binding ?: throw RuntimeException("FragmentNoteListBinding equals null")

    private lateinit var finishApp: FinishApp
    private var isSelectedEntries = false

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
        initBottomAppBar()
        initRecyclerView()
        initEntryClickListeners()
        initColorButtons()
        initBackPressed()
        initNoteList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeNoteList() {
        viewModel.noteList.observe(viewLifecycleOwner) {
            adapterNoteList.submitList(it)
        }
    }

    private fun initNoteList() {
        viewModel.getNoteList()
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
            if (isSelectedEntries) {
                showColorButtons()
            }
        }
        binding.ibNoteListDelete.setOnClickListener {
            removeEntriesFromNote()
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
    private fun removeEntriesFromNote() {
        if (isSelectedEntries) {
            Snackbar.make(
                requireActivity().findViewById(R.id.main_container),
                resources.getString(R.string.confirm_deletion_entries),
                Snackbar.LENGTH_LONG
            )
                .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        viewModel.resetSelectedEntries()
                    }
                })
                .setAction(R.string.snack_bar_note_list) {
                    viewModel.removeEntriesFromNote()
                }
                .show()
        }
    }

    private fun initRecyclerView() {
        val noteListRecyclerView: RecyclerView = binding.recyclerView
        noteListRecyclerView.adapter = adapterNoteList
    }

    private fun initEntryClickListeners() {
        // Get an indicator whether there are selected elements collections
        viewModel.isSelected.observe(viewLifecycleOwner) {
            isSelectedEntries = it
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
        adapterNoteList.onEntryClick = {
            if (!isSelectedEntries) {
                // Go to NoteEntryFragment
                launchNoteEntryFragment(it)
            } else {
                // Deselecting elements collections
                viewModel.selectEntriesAtNote(it)
            }
        }
        adapterNoteList.onEntryLongClick = {
            // Selecting elements collections
            viewModel.selectEntriesAtNote(it)
        }
    }

    private fun initColorButtons() {
        val shadeColor = if (isNight) EntriesColors.DARK_COLOR
        else EntriesColors.LIGHT_COLOR
        with(binding) {
            mbNoteListColorPink?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.PINK]))
            mbNoteListColorPurple?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.PURPLE]))
            mbNoteListColorIndigo?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.INDIGO]))
            mbNoteListColorGreen?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.GREEN]))
            mbNoteListColorOrange?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.ORANGE]))
            mbNoteListColorBrown?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.BROWN]))
            mbNoteListColorGray?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.GRAY]))
            mbNoteListColorBlueGray?.backgroundTintList = ColorStateList.valueOf(requireContext()
                .getColor(EntriesColors.entriesColor[shadeColor][EntriesColors.BLUE_GRAY]))
            mbNoteListColorPink?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.PINK)
                hideColorButtons()
            }
            mbNoteListColorPink?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.PINK)
                hideColorButtons()
            }
            mbNoteListColorPurple?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.PURPLE)
                hideColorButtons()
            }
            mbNoteListColorIndigo?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.INDIGO)
                hideColorButtons()
            }
            mbNoteListColorGreen?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.GREEN)
                hideColorButtons()
            }
            mbNoteListColorOrange?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.ORANGE)
                hideColorButtons()
            }
            mbNoteListColorBrown?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.BROWN)
                hideColorButtons()
            }
            mbNoteListColorGray?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.GRAY)
                hideColorButtons()
            }
            mbNoteListColorBlueGray?.setOnClickListener {
                viewModel.setColorIndex(EntriesColors.BLUE_GRAY)
                hideColorButtons()
            }
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

    private fun launchNoteEntryFragment(noteEntry: NoteEntry) {
        val timeStamp = noteEntry.timeStamp
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
            .replace(R.id.main_container, NoteEntryViewFragment.newInstance(timeStamp))
            .addToBackStack(NoteEntryViewFragment.NAME)
            .commit()
    }


    // needed to close the application on the button back
    private fun retryNoteListFragment() {
        requireActivity().supportFragmentManager.popBackStack(NAME, 1)
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

        @JvmStatic
        fun newInstance() = NoteListFragment()
    }

    private fun showPinkButton() {
        val mb = binding.mbNoteListColorPink
        showColorFab(mb)
    }

    private fun hidePinkButton() {
        val mb = binding.mbNoteListColorPink
        hideColorFab(mb)
    }

    private fun showPurpleButton() {
        val mb = binding.mbNoteListColorPurple
        showColorFab(mb)
    }

    private fun hidePurpleButton() {
        val mb = binding.mbNoteListColorPurple
        hideColorFab(mb)
    }

    private fun showIndigoButton() {
        val mb = binding.mbNoteListColorIndigo
        showColorFab(mb)
    }

    private fun hideIndigoButton() {
        val mb = binding.mbNoteListColorIndigo
        hideColorFab(mb)
    }

    private fun showGreenButton() {
        val mb = binding.mbNoteListColorGreen
        showColorFab(mb)
    }

    private fun hideGreenButton() {
        val mb = binding.mbNoteListColorGreen
        hideColorFab(mb)
    }

    private fun showOrangeButton() {
        val mb = binding.mbNoteListColorOrange
        showColorFab(mb)
    }

    private fun hideOrangeButton() {
        val mb = binding.mbNoteListColorOrange
        hideColorFab(mb)
    }

    private fun showBrownButton() {
        val mb = binding.mbNoteListColorBrown
        showColorFab(mb)
    }

    private fun hideBrownButton() {
        val mb = binding.mbNoteListColorBrown
        hideColorFab(mb)
    }

    private fun showGrayButton() {
        val mb = binding.mbNoteListColorGray
        showColorFab(mb)
    }

    private fun hideGrayButton() {
        val mb = binding.mbNoteListColorGray
        hideColorFab(mb)
    }

    private fun showBlueGrayButton() {
        val mb = binding.mbNoteListColorBlueGray
        showColorFab(mb)
    }

    private fun hideBlueGrayButton() {
        val mb = binding.mbNoteListColorBlueGray
        hideColorFab(mb)
    }

    private fun showColorFab(mb: MaterialButton?) {
        val showButton = AnimationUtils.loadAnimation(requireContext(), R.anim.show_color_button)
        val layoutParams = mb?.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.marginEnd += (mb.width * 1.05).toInt()
        mb.layoutParams = layoutParams
        mb.startAnimation(showButton)
        mb.visibility = View.VISIBLE
        mb.isClickable = true
    }

    private fun hideColorFab(mb: MaterialButton?) {
        val hideButton = AnimationUtils.loadAnimation(requireContext(), R.anim.hide_color_button)
        val layoutParams = mb?.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.marginEnd -= (mb.width * 1.05).toInt()
        mb.layoutParams = layoutParams
        mb.startAnimation(hideButton)
        mb.visibility = View.INVISIBLE
        mb.isClickable = false
    }

}