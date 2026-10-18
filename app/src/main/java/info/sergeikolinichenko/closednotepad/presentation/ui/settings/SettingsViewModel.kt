package info.sergeikolinichenko.closednotepad.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Settings
import info.sergeikolinichenko.closednotepad.presentation.di.AppCoroutineScope
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import info.sergeikolinichenko.closednotepad.usecases.TrashPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val notesRepository: NotesRepository,
    @AppCoroutineScope private val appScope: CoroutineScope
) : ViewModel() {

    val settings: StateFlow<Settings> = preferencesRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), preferencesRepository.current())

    fun setDefaultColor(index: Int) = preferencesRepository.setDefaultColorIndex(index)

    fun setDaysBeforeDeletion(days: Int) {
        preferencesRepository.setDaysBeforeDeletion(days)
        // The new period applies right away, not only on the next daily cleanup
        TrashPolicy.deleteBefore(System.currentTimeMillis(), days)?.let { time ->
            appScope.launch { notesRepository.deleteTrashedBefore(time) }
        }
    }
}
