package info.sergeikolinichenko.closednotepad.presentation.screens

import android.animation.Animator
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.databinding.FragmentNotesMenuBinding
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesmenu.ViewModelNotesMenu
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesmenu.ViewModelNotesMenuFactory
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl

class NotesMenuFragment : Fragment() {

    private var _binding: FragmentNotesMenuBinding? = null
    private val binding: FragmentNotesMenuBinding
        get() = _binding ?: throw RuntimeException("FragmentSettingsBinding equals null")

    private val viewModelFactory by lazy {
        ViewModelNotesMenuFactory(requireActivity().application)
    }
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ViewModelNotesMenu::class.java]
    }

    private var isNight = false
    private var behaviorColorButtons = BottomSheetBehavior<ConstraintLayout>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDefaultColorIndex()
        observeShowColorButtons()
        observeDaysBeforeDelete()
        observeDaySetButtons()
        isNightMode()
        initSettingsButtons()
        initFabExit()
        initColorButtons()
        initDaySetButtons()
        initBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeDefaultColorIndex() {
        viewModel.defaultColorIndex.observe(viewLifecycleOwner) {
            val colorNote: Int = if (it != PreferencesRepositoryImpl.ERROR_GET_INT) {
                if (isNight) NoteColors.noteColor[NoteColors.DARK_COLOR][it]
                else NoteColors.noteColor[NoteColors.LIGHT_COLOR][it]
            } else {
                if (isNight) NoteColors.noteColor[NoteColors.DARK_COLOR][NoteColors.GRAY]
                else NoteColors.noteColor[NoteColors.LIGHT_COLOR][NoteColors.GRAY]
            }
            binding.mbMenuDefaultColor.backgroundTintList =
                resources.getColorStateList(colorNote, null)
        }
    }

    private fun observeShowColorButtons() {
        viewModel.showColorButtons.observe(viewLifecycleOwner) {
            if (it) showColorButtons()
            else hideColorButtons()
        }
    }

    private fun observeDaysBeforeDelete() {
        viewModel.daysBeforeDelete.observe(viewLifecycleOwner) {
            val daysPref = it
            val daysBefore = if (daysPref != null && daysPref > 0) daysPref.toString()
            else resources.getString(R.string.no_days_before_deletion)
            val string = resources.getString(R.string.days_before_deletion)

            val text = "$daysBefore $string"
            binding.mbMenuDaysBeforeDelete.text = text
        }
    }

    private fun observeDaySetButtons() {
        viewModel.showDaySetButtons.observe(viewLifecycleOwner) {
            if (it) showDaySetButtons()
            else hideDaySetButtons()
        }
    }

    private fun initSettingsButtons() {
        with(binding) {
            if (isNight) {
                mbMenuDefaultColor.setIconResource(
                    R.drawable.ic_palette_white_48dp
                )
                mbMenuDaysBeforeDelete.setIconResource(
                    R.drawable.ic_delete_clock_outline_white_36dp
                )
                mbMenuWastebasket.setIconResource(
                    R.drawable.ic_delete_white_48dp
                )
            } else {
                mbMenuDefaultColor.setIconResource(
                    R.drawable.ic_palette_black_48dp
                )
                mbMenuDaysBeforeDelete.setIconResource(
                    R.drawable.ic_delete_clock_outline_black_36dp
                )
                mbMenuWastebasket.setIconResource(
                    R.drawable.ic_delete_black_48dp
                )
            }

            mbMenuDaysBeforeDelete.setOnClickListener {
                viewModel.showSetDaysButton()
            }
            mbMenuDefaultColor.setOnClickListener {
                viewModel.showColorButtons()
            }
            mbMenuWastebasket.setOnClickListener {
                launchTrashCanListFragment()
            }
        }
    }

    private fun initFabExit() {
        binding.fabNotesMenuExit.setOnClickListener {
            retryPreviousFragment()
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
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.PINK])
            )
            mbSettingsPurple.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.PURPLE])
            )
            mbSettingsIndigo.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.INDIGO])
            )
            mbSettingsGreen.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.GREEN])
            )
            mbSettingsOrange.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.ORANGE])
            )
            mbSettingsBrown.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.BROWN])
            )
            mbSettingsGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.GRAY])
            )
            mbSettingsBlueGray.backgroundTintList = ColorStateList.valueOf(
                requireContext().getColor(NoteColors.noteColor[shadeColor][NoteColors.BLUE_GRAY])
            )

            mbSettingsPink.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.PINK)
            }
            mbSettingsPurple.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.PURPLE)
            }
            mbSettingsIndigo.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.INDIGO)
            }
            mbSettingsGreen.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.GREEN)
            }
            mbSettingsOrange.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.ORANGE)
            }
            mbSettingsBrown.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.BROWN)
            }
            mbSettingsGray.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.GRAY)
            }
            mbSettingsBlueGray.setOnClickListener {
                viewModel.setDefaultColor(NoteColors.BLUE_GRAY)
            }
        }
    }

    private fun initDaySetButtons() {
        with(binding) {
            mbMenuDaySetNo.setOnClickListener {
                viewModel.setDaysBeforeDelete(DAYS_BEFORE_DELETE_0)
            }
            mbMenuDaySet10.setOnClickListener {
                viewModel.setDaysBeforeDelete(DAYS_BEFORE_DELETE_10)
            }
            mbMenuDaySet30.setOnClickListener {
                viewModel.setDaysBeforeDelete(DAYS_BEFORE_DELETE_30)
            }
            mbMenuDaySet90.setOnClickListener {
                viewModel.setDaysBeforeDelete(DAYS_BEFORE_DELETE_90)
            }
            mbMenuDaySet180.setOnClickListener {
                viewModel.setDaysBeforeDelete(DAYS_BEFORE_DELETE_180)
            }
            mbMenuDaySet360.setOnClickListener {
                viewModel.setDaysBeforeDelete(DAYS_BEFORE_DELETE_360)
            }
        }
    }

    private fun showDaySetButtons() {
        binding.mbMenuDaysBeforeDelete.animate()
            .scaleY(0f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    binding.mbMenuDaysBeforeDelete.visibility = View.INVISIBLE
                    binding.mbMenuDaysBeforeDelete.isClickable = false
                    binding.clMenuDaySet.animate()
                        .scaleY(1F)
                        .setDuration(250)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator?) {
                                binding.clMenuDaySet.visibility = View.VISIBLE
                            }

                            override fun onAnimationEnd(p0: Animator?) {
                                with(binding) {
                                    mbMenuDaySet10.isClickable = true
                                    mbMenuDaySet30.isClickable = true
                                    mbMenuDaySet90.isClickable = true
                                    mbMenuDaySet180.isClickable = true
                                    mbMenuDaySet360.isClickable = true
                                }
                            }

                            override fun onAnimationCancel(p0: Animator?) {
                            }

                            override fun onAnimationRepeat(p0: Animator?) {
                            }
                        })
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
    }

    private fun hideDaySetButtons() {
        binding.clMenuDaySet.animate()
            .scaleY(0f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                    with(binding) {
                        mbMenuDaySet10.isClickable = false
                        mbMenuDaySet30.isClickable = false
                        mbMenuDaySet90.isClickable = false
                        mbMenuDaySet180.isClickable = false
                        mbMenuDaySet360.isClickable = false
                    }
                }

                override fun onAnimationEnd(p0: Animator?) {
                    binding.mbMenuDaysBeforeDelete.visibility = View.VISIBLE
                    binding.mbMenuDaysBeforeDelete.animate()
                        .scaleY(1F)
                        .setDuration(250)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator?) {
                                binding.clMenuDaySet.visibility = View.VISIBLE
                            }
                            override fun onAnimationEnd(p0: Animator?) {
                                binding.mbMenuDaysBeforeDelete.isClickable = true
                            }

                            override fun onAnimationCancel(p0: Animator?) {
                            }

                            override fun onAnimationRepeat(p0: Animator?) {
                            }
                        })
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
    }

    private fun initBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    retryPreviousFragment()
                }

            }
        )
    }

    private fun launchTrashCanListFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, TrashCanListFragment.newInstance())
            .addToBackStack(TrashCanListFragment.NAME)
            .commit()
    }

    private fun isNightMode() {
        isNight = (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES)
    }

    private fun retryPreviousFragment() {
        requireActivity().supportFragmentManager.popBackStack(
            null,
            0
        )
    }

    companion object {

        const val NAME = "settings_fragment"

        private const val DAYS_BEFORE_DELETE_0 = 0
        private const val DAYS_BEFORE_DELETE_10 = 10
        private const val DAYS_BEFORE_DELETE_30 = 30
        private const val DAYS_BEFORE_DELETE_90 = 90
        private const val DAYS_BEFORE_DELETE_180 = 180
        private const val DAYS_BEFORE_DELETE_360 = 360

        @JvmStatic
        fun newInstance() = NotesMenuFragment()
    }
}
