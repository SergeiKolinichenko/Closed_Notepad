package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
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
        isNightMode()
        initToolbar()
        initBottomAppBar()
        initNoteList()
        initRecyclerView()
        initEntryClickListeners()
        initBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initNoteList() {
        viewModel.noteList.observe(viewLifecycleOwner) {
            adapterNoteList.submitList(it)
        }
        viewModel.getNoteList()
    }

    private fun initToolbar() {
        if (isNight){
            binding.ibNoteListFilter?.setImageResource(R.drawable.ic_filter_variant_white_36dp)
        } else {
            binding.ibNoteListFilter?.setImageResource(R.drawable.ic_filter_variant_black_36dp)
        }
        binding.ivOutlineNoteList?.setOnClickListener {
            finishApp.finishApp()
        }
    }

    private fun initBottomAppBar(){
        if (isNight) {
            binding.ibNoteListCog?.setImageResource(R.drawable.ic_cog_white_36dp)
            binding.ibNoteListMagnify?.setImageResource(R.drawable.ic_magnify_white_36dp)
        } else {
            binding.ibNoteListCog?.setImageResource(R.drawable.ic_cog_black_36dp)
            binding.ibNoteListMagnify?.setImageResource(R.drawable.ic_magnify_black_36dp)
        }
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
                        viewModel.resSelEntriesAtNote()
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
        }
        adapterNoteList.onEntryClick = {
            if (!isSelectedEntries) {
                // Go to NoteEntryFragment
                launchNoteEntryFragment(it)
            } else {
                // Deselecting elements collections
                viewModel.resSelEntriesAtNote(it)
            }
        }
        adapterNoteList.onEntryLongClick = {
            // Selecting elements collections
            viewModel.selectEntriesAtNote(it)
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

}