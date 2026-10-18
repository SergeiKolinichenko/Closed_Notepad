package info.sergeikolinichenko.closednotepad.presentation.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessage
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessages
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashState(
    val notes: List<Note> = emptyList(),
    val selected: Set<Long> = emptySet(),
    val daysBeforeDeletion: Int = 0,
    val loaded: Boolean = false
) {
    val selectedNotes: List<Note>
        get() = notes.filter { it.id in selected }
}

class TrashViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    preferencesRepository: PreferencesRepository,
    private val uiMessages: UiMessages
) : ViewModel() {

    private val selected = MutableStateFlow<Set<Long>>(emptySet())

    val state: StateFlow<TrashState> = combine(
        notesRepository.observeTrash(),
        preferencesRepository.settings,
        selected
    ) { notes, settings, selected ->
        val ids = notes.mapTo(HashSet()) { it.id }
        TrashState(notes, selected.filterTo(HashSet()) { it in ids }, settings.daysBeforeDeletion, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrashState())

    fun toggleSelection(id: Long) = selected.update { if (id in it) it - id else it + id }

    fun clearSelection() {
        selected.value = emptySet()
    }

    fun restore(ids: Set<Long>) {
        viewModelScope.launch {
            notesRepository.restore(ids)
            uiMessages.show(UiMessage(R.plurals.restored, count = ids.size))
        }
        clearSelection()
    }

    fun deleteForever(ids: Set<Long>) {
        viewModelScope.launch { notesRepository.deleteForever(ids) }
        clearSelection()
    }

    fun emptyTrash() {
        viewModelScope.launch { notesRepository.emptyTrash() }
        clearSelection()
    }
}
