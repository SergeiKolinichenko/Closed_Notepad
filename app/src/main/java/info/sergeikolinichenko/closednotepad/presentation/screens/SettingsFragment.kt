package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentSettingsBinding
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.settins.ViewModelSettings
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.settins.ViewModelSettingsFactory
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding ?: throw RuntimeException("FragmentSettingsBinding equals null")

    private val viewModelFactory by lazy {
        ViewModelSettingsFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelSettings::class.java]
    }

    private var isNight = false
    private var behaviorColorButtons = BottomSheetBehavior<ConstraintLayout>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDefaultColorIndex()
        isNightMode()
        initSettingsButtons()
        initColorButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeDefaultColorIndex() {
        viewModel.defaultColorIndex.observe(viewLifecycleOwner) {
            val colorNote: Int = if (it != PreferencesRepositoryImpl.ERROR_GET_INT) {
                if (isNight) NoteColors.entriesColor[NoteColors.DARK_COLOR][it]
                else NoteColors.entriesColor[NoteColors.LIGHT_COLOR][it]
            } else {
                if (isNight) NoteColors.entriesColor[NoteColors.DARK_COLOR][NoteColors.GRAY]
                else NoteColors.entriesColor[NoteColors.LIGHT_COLOR][NoteColors.GRAY]
            }
            binding.mbSettingsDefaultColor.iconTint =
                resources.getColorStateList(colorNote, null)
        }
    }

    private fun initSettingsButtons() {
        with(binding) {
            if (isNight) {
                mbSettingsDefaultColor.setIconResource(R.drawable.ic_palette_white_48dp)
                mbSettingsWastebasket.setIconResource(R.drawable.ic_delete_white_48dp)
            } else {
                mbSettingsDefaultColor.setIconResource(R.drawable.ic_palette_black_48dp)
                mbSettingsWastebasket.setIconResource(R.drawable.ic_delete_black_48dp)
            }
            mbSettingsDefaultColor.setOnClickListener {
                showColorButtons()
            }
        }
    }

    private fun showColorButtons() {
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_HIDDEN) {
            behaviorColorButtons.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun hideColorButtons() {
        if (behaviorColorButtons.state == BottomSheetBehavior.STATE_EXPANDED) {
            behaviorColorButtons.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun initColorButtons() {
        behaviorColorButtons = BottomSheetBehavior.from(binding.clSettingsColorButtons)
        behaviorColorButtons.state = BottomSheetBehavior.STATE_HIDDEN

        val shadeColor = if (isNight) NoteColors.DARK_COLOR
        else NoteColors.LIGHT_COLOR

        with(binding) {
            mbSettingsPink.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.PINK])
            )
            mbSettingsPurple.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.PURPLE])
            )
            mbSettingsIndigo.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.INDIGO])
            )
            mbSettingsGreen.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.GREEN])
            )
            mbSettingsOrange.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.ORANGE])
            )
            mbSettingsBrown.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.BROWN])
            )
            mbSettingsGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.GRAY])
            )
            mbSettingsBlueGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.entriesColor[shadeColor][NoteColors.BLUE_GRAY])
            )

            mbSettingsPink.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.PINK)
                hideColorButtons()
            }
            mbSettingsPurple.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.PURPLE)
                hideColorButtons()
            }
            mbSettingsIndigo.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.INDIGO)
                hideColorButtons()
            }
            mbSettingsGreen.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.GREEN)
                hideColorButtons()
            }
            mbSettingsOrange.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.ORANGE)
                hideColorButtons()
            }
            mbSettingsBrown.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.BROWN)
                hideColorButtons()
            }
            mbSettingsGray.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.GRAY)
                hideColorButtons()
            }
            mbSettingsBlueGray.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.BLUE_GRAY)
                hideColorButtons()
            }
        }
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
    }

    companion object {

        const val NAME = "settings_fragment"

        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}