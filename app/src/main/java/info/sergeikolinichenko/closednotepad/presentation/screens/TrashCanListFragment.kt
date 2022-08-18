package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
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
        observeTrashcanList()
        isNightMode()
        initRecyclerView()
        initFabExit()
        initBackPressed()
    }

    private fun observeTrashcanList() {
        viewModel.removedNoteList.observe(viewLifecycleOwner) {
            adapterTrashCanList.submitList(it)
        }
    }

    private fun orderViewTrashcanList(list: List<RemovedNote>): List<RemovedNote> {
        return list.sortedBy { it.timeStamp }
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
        adapterTrashCanList.isNight = isNight
    }

    private fun initRecyclerView() {
        val noteListRecyclerView: RecyclerView = binding.recyclerViewTrashCan
        noteListRecyclerView.adapter = adapterTrashCanList
    }

    private fun initFabExit() {
        binding.fabTrashCanListExit.setOnClickListener {
            retryNoteListFragment()
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

    private fun retryNoteListFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0
        )
    }

    companion object {

        const val NAME = "trash_can_list_fragment"

        @JvmStatic
        fun newInstance() = TrashCanListFragment()
    }
}