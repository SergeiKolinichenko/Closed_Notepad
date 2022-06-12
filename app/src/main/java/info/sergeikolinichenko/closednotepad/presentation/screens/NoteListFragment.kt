package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteListBinding
import info.sergeikolinichenko.closednotepad.presentation.adapters.notelist.NoteListAdapter
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteList
import java.util.concurrent.TimeoutException

class NoteListFragment : Fragment() {

    private val toolbar by lazy {binding.toolBar }

    private val viewModel by lazy {ViewModelProvider(this)[ViewModelNoteList::class.java]}
    private val adapterNoteList by lazy { NoteListAdapter() }

    private var _binding: FragmentNoteListBinding? = null
    private val binding: FragmentNoteListBinding
    get() = _binding ?: throw RuntimeException("FragmentNoteListBinding equals null")

    // screen mode, title, detail, square or big square
    private var screenMode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            screenMode = it.getInt(SCREEN_NODE)
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

        initToolbar()
        initRecyclerView()
        initEntryClickListener()
        initNoteList()
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
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.title = getString(R.string.title_note_list)
    }

    private fun initRecyclerView() {
        val noteListRecyclerView: RecyclerView = binding.recyclerView
        initItemTouchHelper(noteListRecyclerView)
        with(noteListRecyclerView){
            adapter = adapterNoteList
        }
    }

    private fun initEntryClickListener() {
        adapterNoteList.onEntryClick = {
            Toast.makeText(requireContext(), "onEntryClickListener ${it.itselfEntry}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initItemTouchHelper(recyclerView: RecyclerView) {
        val callBack = object: ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.END or ItemTouchHelper.START
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //Log.d("MyLog", "direction = $direction")
                //Log.d("MyLog", "position = ${viewHolder.layoutPosition}")
                val item = adapterNoteList.currentList[viewHolder.adapterPosition]
                viewModel.removeEntryFromNote(item)
            }
        }
        val itemTouchHelper = ItemTouchHelper(callBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    companion object {
        const val SCREEN_MODE_TITLE = 0
        private const val SCREEN_NODE = "screen_mode"
        @JvmStatic
        fun newInstance() =
            NoteListFragment().apply {
                arguments = Bundle().apply {
                    putInt(SCREEN_NODE, SCREEN_MODE_TITLE)
                }
            }
    }
}