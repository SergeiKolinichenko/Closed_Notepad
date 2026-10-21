package info.sergeikolinichenko.closednotepad.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.models.NotesLayout
import info.sergeikolinichenko.closednotepad.models.Settings
import info.sergeikolinichenko.closednotepad.models.SortOrder
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessage
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessages
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import info.sergeikolinichenko.closednotepad.usecases.NoteSorter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class NoteFilter(val type: NoteType?) {
    ALL(null), TEXT(NoteType.TEXT), CHECKLIST(NoteType.CHECKLIST), SHOPPING(NoteType.SHOPPING)
}

data class HomeState(
    val notes: List<Note> = emptyList(),
    val hasAnyNotes: Boolean = false,
    val filter: NoteFilter = NoteFilter.ALL,
    val settings: Settings = Settings(),
    val selected: Set<Long> = emptySet(),
    val loaded: Boolean = false
) {
    val selectedNotes: List<Note>
        get() = notes.filter { it.id in selected }
}

class HomeViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val preferencesRepository: PreferencesRepository,
    private val uiMessages: UiMessages
) : ViewModel() {

    private val filter = MutableStateFlow(NoteFilter.ALL)
    private val selected = MutableStateFlow<Set<Long>>(emptySet())

    val state: StateFlow<HomeState> = combine(
        notesRepository.observeNotes(),
        preferencesRepository.settings,
        filter,
        selected
    ) { notes, settings, filter, selected ->
        val visible = notes.filter { filter.type == null || it.type == filter.type }
        val ids = notes.mapTo(HashSet()) { it.id }
        HomeState(
            notes = NoteSorter.sort(visible, settings.sortOrder),
            hasAnyNotes = notes.isNotEmpty(),
            filter = filter,
            settings = settings,
            selected = selected.filterTo(HashSet()) { it in ids },
            loaded = true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun createNote(type: NoteType, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val note = Note.blank(
                type = type,
                colorIndex = preferencesRepository.current().defaultColorIndex,
                now = System.currentTimeMillis(),
                newItemId = { UUID.randomUUID().toString() }
            )
            onCreated(notesRepository.createNote(note))
        }
    }

    // A short note written right on the home screen, without opening the editor
    fun saveQuickNote(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            notesRepository.createNote(
                Note(
                    type = NoteType.TEXT,
                    body = text.trim(),
                    colorIndex = preferencesRepository.current().defaultColorIndex,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun setFilter(value: NoteFilter) {
        filter.value = value
        clearSelection()
    }

    fun setLayout(layout: NotesLayout) = preferencesRepository.setLayout(layout)

    fun setSortOrder(order: SortOrder) = preferencesRepository.setSortOrder(order)

    fun toggleSelection(id: Long) {
        selected.update { if (id in it) it - id else it + id }
    }

    fun clearSelection() {
        selected.value = emptySet()
    }

    fun pinSelected() {
        val notes = state.value.selectedNotes
        val pin = notes.any { !it.isPinned }
        viewModelScope.launch { notesRepository.setPinned(notes.map { it.id }, pin) }
        clearSelection()
    }

    fun setColorOfSelected(colorIndex: Int) {
        val ids = state.value.selected
        viewModelScope.launch { notesRepository.setColor(ids, colorIndex) }
        clearSelection()
    }

    fun trashSelected() {
        val ids = state.value.selected
        if (ids.isEmpty()) return
        viewModelScope.launch {
            notesRepository.moveToTrash(ids, System.currentTimeMillis())
            uiMessages.show(
                UiMessage(R.plurals.moved_to_trash, count = ids.size, undo = { notesRepository.restore(ids) })
            )
        }
        clearSelection()
    }
}
