package info.sergeikolinichenko.closednotepad.presentation.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Settings
import info.sergeikolinichenko.closednotepad.presentation.di.AppCoroutineScope
import info.sergeikolinichenko.closednotepad.presentation.transfer.NotesTransfer
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessage
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessages
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import info.sergeikolinichenko.closednotepad.usecases.TrashPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val notesRepository: NotesRepository,
    private val notesTransfer: NotesTransfer,
    private val uiMessages: UiMessages,
    @AppCoroutineScope private val appScope: CoroutineScope
) : ViewModel() {

    // Locked notes are left out of an export unless the user asks for them
    val lockedCount: StateFlow<Int> = notesRepository.observeNotes()
        .map { notes -> notes.count { it.isLocked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _transferring = MutableStateFlow(false)
    val transferring: StateFlow<Boolean> = _transferring.asStateFlow()

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

    // Runs in the app scope, so leaving the screen doesn't stop a half-written export
    fun export(treeUri: Uri, includeLocked: Boolean) = transfer {
        val count = notesTransfer.export(treeUri, includeLocked)
        UiMessage(R.plurals.exported_notes, count = count)
    }

    fun import(uris: List<Uri>) {
        if (uris.isEmpty()) return
        transfer {
            val count = notesTransfer.import(uris)
            if (count == 0) UiMessage(R.string.import_nothing_new) else UiMessage(R.plurals.imported_notes, count = count)
        }
    }

    private fun transfer(block: suspend () -> UiMessage) {
        _transferring.value = true
        appScope.launch {
            val message = runCatching { block() }.getOrElse { UiMessage(R.string.transfer_failed) }
            _transferring.value = false
            uiMessages.show(message)
        }
    }
}
