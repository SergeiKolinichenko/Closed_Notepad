package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteSearchBinding
import info.sergeikolinichenko.closednotepad.presentation.NotesApp
import info.sergeikolinichenko.closednotepad.presentation.adapters.notesearch.NoteSearchAdapter
import info.sergeikolinichenko.closednotepad.presentation.utils.authUser
import info.sergeikolinichenko.closednotepad.presentation.utils.readinessCheckBiometric
import info.sergeikolinichenko.closednotepad.presentation.utils.showSnakebar
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteSearch
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNotesFactory
import javax.inject.Inject

/**
Search note in database
create 07.2022 by Sergei Kolinichenko
 **/

class NoteSearchFragment : Fragment() {

    private var _binding: FragmentNoteSearchBinding? = null
    private val binding: FragmentNoteSearchBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteSearchBinding equals null")

    @Inject
    lateinit var viewModelFactory: ViewModelNotesFactory
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteSearch::class.java]
    }

    private val adapterSearchNote by lazy { NoteSearchAdapter() }

    private var isNight = false

    private val component by lazy { (requireActivity().application as NotesApp).component }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteSearchBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSearchView()
        initFabExit()
        initNoteClickListener()
        isNightMode()
        observeNoteList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
        adapterSearchNote.isNight = isNight
    }

    private fun observeNoteList() {
        viewModel.locNoteList.observe(viewLifecycleOwner) {
            adapterSearchNote.submitList(it)
        }
    }

    private fun initRecyclerView() {
        val rvSearchNote: RecyclerView = binding.rvSearchNote
        rvSearchNote.adapter = adapterSearchNote
    }

    private fun initNoteClickListener() {
        adapterSearchNote.onNoteClick = {
            // Go to NoteViewFragment
            if (it.isLocked) getBiometricSuccess(it.timeStamp, ::launchNoteViewFragment)
            else launchNoteViewFragment(it.timeStamp)
        }
    }

    private fun initFabExit() {
        binding.fabNoteSearchExit.setOnClickListener {
            retryNoteListFragment()
        }
    }

    private fun initSearchView() {
        binding.svSearchNote.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (newText.isNotEmpty()) {
                        viewModel.searchNote(it)
                    } else {
                        adapterSearchNote.submitList(null)
                    }
                }
                return false
            }
        })
    }

    private fun Fragment.hideSoftKeyboard() {
        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun retryNoteListFragment() {
        hideSoftKeyboard()
        requireActivity().supportFragmentManager.popBackStack(
            NoteListFragment.NAME,
            0
        )
    }

    private fun getBiometricSuccess(timeStamp: Long, act: (tm: Long) -> Unit) {
        val check = readinessCheckBiometric(
            this,
            BiometricManager.from(requireActivity().application),
            binding.fabNoteSearchExit,
            isNight,
            ::showSnakebar
        )
        if (check) {
            authUser(
                this,
                ContextCompat.getMainExecutor(requireActivity().application),
                binding.fabNoteSearchExit,
                isNight,
                timeStamp,
                act,
                ::showSnakebar
            )
        }
    }

    private fun launchNoteViewFragment(timeStamp: Long) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_bottom, R.anim.exit_to_top,
                R.anim.enter_from_top, R.anim.exit_to_bottom
            )
            .replace(R.id.main_container, NoteViewFragment.newInstance(timeStamp))
            .addToBackStack(NoteViewFragment.NAME)
            .commit()
    }

    companion object {
        const val NAME = "search_note_fragment"

        @JvmStatic
        fun newInstance() = NoteSearchFragment()
    }

}