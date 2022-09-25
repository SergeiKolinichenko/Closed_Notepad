package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNoteSearchBinding
import info.sergeikolinichenko.closednotepad.presentation.adapters.notesearch.NoteSearchAdapter
import info.sergeikolinichenko.closednotepad.presentation.utils.BiometricVerification
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesearch.ViewModelNoteSearch
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesearch.ViewModelNoteSearchFactory

class NoteSearchFragment : Fragment() {

    private var _binding: FragmentNoteSearchBinding? = null
    private val binding: FragmentNoteSearchBinding
        get() = _binding ?: throw RuntimeException("FragmentNoteSearchBinding equals null")

    private val viewModelFactory by lazy {
        ViewModelNoteSearchFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNoteSearch::class.java]
    }

    private val adapterSearchNote by lazy { NoteSearchAdapter() }

    private var isNight = false

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

        val biometricVerification = BiometricVerification(this)

        if (biometricVerification.readinessCheckBiometric(::showSnakebar)) {
            biometricVerification.authUser(timeStamp, act, ::showSnakebar)
        }
    }

    private fun showSnakebar(message: String) {
        val icon = if (isNight) R.drawable.ic_information_variant_black_48dp
        else R.drawable.ic_information_variant_white_48dp

        val snackBar = Snackbar.make(
            requireActivity().findViewById(R.id.main_container),
            message,
            Snackbar.LENGTH_LONG
        )

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
        snackBar.anchorView = binding.fabNoteSearchExit
        snackBar.show()
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