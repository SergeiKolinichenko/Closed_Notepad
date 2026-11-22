package info.sergeikolinichenko.closednotepad.presentation.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.presentation.di.AppCoroutineScope
import info.sergeikolinichenko.closednotepad.presentation.security.NoteLock
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessage
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessages
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.security.NotesVault
import info.sergeikolinichenko.closednotepad.usecases.NoteConverter
import info.sergeikolinichenko.closednotepad.usecases.ShoppingInputParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

sealed interface EditorState {
    data object Loading : EditorState
    data object NotFound : EditorState
    data class Ready(val note: Note) : EditorState
}

// Edits one note. A note that was just created is saved automatically a moment
// after every change, and the last change is saved even when the screen is closed.
// An existing note keeps its changes in memory until the user confirms them:
// with the save button or with "Yes" in the dialog shown on leaving the note.
// Changes that were not confirmed are lost if the system kills the app in the
// background; they are never written anywhere unencrypted.
@OptIn(FlowPreview::class)
class EditorViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val noteLock: NoteLock,
    private val vault: NotesVault,
    private val uiMessages: UiMessages,
    @AppCoroutineScope private val appScope: CoroutineScope
) : ViewModel() {

    private val _state = MutableStateFlow<EditorState>(EditorState.Loading)
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _saved = MutableStateFlow(true)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    // An existing note differs from its saved version
    private val _hasChanges = MutableStateFlow(false)
    val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // Id of an item that was just added, so the screen can move the cursor to it
    private val _focusItemId = MutableStateFlow<String?>(null)
    val focusItemId: StateFlow<String?> = _focusItemId.asStateFlow()

    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var noteId: Long? = null
    @Volatile
    private var dirty = false
    @Volatile
    private var closed = false
    // True for a note that was just created, false for an existing one
    private var autoSave = true
    // The saved version of an existing note
    private var original: Note? = null

    private val note: Note?
        get() = (_state.value as? EditorState.Ready)?.note

    init {
        viewModelScope.launch {
            changes.debounce(SAVE_DELAY_MILLIS).collect { saveNow() }
        }
    }

    fun start(id: Long, isNew: Boolean) {
        if (noteId != null) return
        noteId = id
        autoSave = isNew
        noteLock.noteOpened(id)
        viewModelScope.launch {
            var loaded = notesRepository.getNote(id)
            _state.value = if (loaded == null || loaded.isInTrash) EditorState.NotFound else EditorState.Ready(loaded)
            original = loaded
            if (loaded?.type == NoteType.SHOPPING) loadSuggestions()
            // An encrypted note is read again once the user has unlocked it
            while (loaded != null && loaded.isSealed) {
                vault.unlocked.first { it }
                loaded = notesRepository.getNote(id)
                if (loaded == null || loaded.isInTrash) {
                    _state.value = EditorState.NotFound
                    return@launch
                }
                if (!loaded.isSealed) {
                    _state.value = EditorState.Ready(loaded)
                    original = loaded
                }
                else vault.unlocked.first { !it }
            }
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _suggestions.value = notesRepository.shoppingSuggestions(SUGGESTIONS_LIMIT)
        }
    }

    private fun update(transform: (Note) -> Note) {
        val current = note ?: return
        // Nothing can be changed before the content of an encrypted note is read
        if (closed || current.isSealed) return
        val changed = transform(current)
        if (changed == current) return
        _state.value = EditorState.Ready(changed.copy(updatedAt = System.currentTimeMillis()))
        if (!autoSave) {
            _hasChanges.value = differsFromOriginal(changed)
            return
        }
        dirty = true
        _saved.value = false
        changes.tryEmit(Unit)
    }

    // The time of the last change alone is not a change
    private fun differsFromOriginal(changed: Note): Boolean {
        val saved = original ?: return true
        return changed.copy(updatedAt = saved.updatedAt) != saved
    }

    // Saves the changes of an existing note and stays in the editor
    fun saveChanges() {
        if (autoSave || closed || !_hasChanges.value) return
        val snapshot = note ?: return
        original = snapshot
        _hasChanges.value = false
        appScope.launch { saveMutex.withLock { notesRepository.saveNote(snapshot) } }
    }

    // "Yes" in the dialog: the changes are saved and the note is closed
    fun saveAndClose(onDone: () -> Unit) {
        val id = noteId
        val snapshot = note
        if (!autoSave && !closed && _hasChanges.value && id != null && snapshot != null) {
            closed = true
            appScope.launch {
                if (snapshot.isBlank) {
                    // An emptied note goes to the trash with its saved content, so it can be restored
                    saveMutex.withLock { notesRepository.moveToTrash(listOf(id), System.currentTimeMillis()) }
                    uiMessages.show(UiMessage(R.plurals.moved_to_trash, count = 1, undo = { notesRepository.restore(listOf(id)) }))
                } else {
                    saveMutex.withLock { notesRepository.saveNote(snapshot) }
                }
            }
        }
        onDone()
    }

    // "No" in the dialog: the changes are dropped and the saved note stays as it was
    fun discardAndClose(onDone: () -> Unit) {
        if (!autoSave) closed = true
        onDone()
    }

    private fun saveNow() {
        if (!dirty || closed) return
        val snapshot = note ?: return
        dirty = false
        appScope.launch {
            saveMutex.withLock { notesRepository.saveNote(snapshot) }
            if (!dirty) _saved.value = true
        }
    }

    fun setTitle(title: String) = update { it.copy(title = title) }

    fun setBody(body: String) = update { it.copy(body = body) }

    fun setColor(index: Int) = update { it.copy(colorIndex = index) }

    fun togglePinned() = update { it.copy(isPinned = !it.isPinned) }

    fun setLocked(locked: Boolean) {
        update { it.copy(isLocked = locked) }
        noteId?.let { if (locked) noteLock.markUnlocked(listOf(it)) }
    }

    // List items

    fun addItem(afterId: String? = null, text: String = "", quantity: String = "") {
        val item = NoteItem(id = UUID.randomUUID().toString(), text = text, quantity = quantity)
        update { note ->
            val index = note.items.indexOfFirst { it.id == afterId }
            val items = note.items.toMutableList()
            if (index >= 0) items.add(index + 1, item) else items.add(firstCheckedIndex(items), item)
            note.copy(items = items)
        }
        if (text.isEmpty()) _focusItemId.value = item.id
    }

    // New items go after the open ones, above the checked ones
    private fun firstCheckedIndex(items: List<NoteItem>): Int {
        val lastOpen = items.indexOfLast { !it.isChecked }
        return lastOpen + 1
    }

    fun focusHandled() {
        _focusItemId.value = null
    }

    fun setItemText(id: String, text: String) = updateItem(id) { it.copy(text = text) }

    fun toggleItem(id: String) = update { note ->
        val item = note.items.firstOrNull { it.id == id } ?: return@update note
        val toggled = item.copy(isChecked = !item.isChecked)
        val rest = note.items.filter { it.id != id }.toMutableList()
        // An item that is unchecked again goes back to the end of the open items
        if (toggled.isChecked) rest.add(toggled) else rest.add(firstCheckedIndex(rest), toggled)
        note.copy(items = rest)
    }

    fun removeItem(id: String) = update { note -> note.copy(items = note.items.filter { it.id != id }) }

    fun replaceItem(item: NoteItem) = updateItem(item.id) { item }

    private fun updateItem(id: String, transform: (NoteItem) -> NoteItem) = update { note ->
        note.copy(items = note.items.map { if (it.id == id) transform(it) else it })
    }

    fun moveItem(fromId: String, toId: String) = update { note ->
        val items = note.items.toMutableList()
        val from = items.indexOfFirst { it.id == fromId }
        val to = items.indexOfFirst { it.id == toId }
        if (from < 0 || to < 0) return@update note
        items.add(to, items.removeAt(from))
        note.copy(items = items)
    }

    // Shopping list

    fun addShoppingInput(input: String, section: String) {
        if (input.isBlank()) return
        val parsed = ShoppingInputParser.parse(input)
        val item = NoteItem(
            id = UUID.randomUUID().toString(),
            text = parsed.name,
            quantity = parsed.quantity,
            section = section.trim()
        )
        update { note ->
            val items = note.items.filter { it.text.isNotBlank() }.toMutableList()
            items.add(firstCheckedIndex(items), item)
            note.copy(items = items)
        }
        _suggestions.value = _suggestions.value.filterNot { it.equals(parsed.name, ignoreCase = true) }
    }

    fun uncheckAll() = update { note -> note.copy(items = note.items.map { it.copy(isChecked = false) }) }

    fun removeChecked() = update { note -> note.copy(items = note.items.filter { !it.isChecked }) }

    // Conversion between text and list

    fun convertType() = update { note ->
        when (note.type) {
            NoteType.TEXT -> NoteConverter.textToList(note) { UUID.randomUUID().toString() }
            NoteType.CHECKLIST, NoteType.SHOPPING -> NoteConverter.listToText(note)
        }
    }

    fun moveToTrash(onDone: () -> Unit) {
        val id = noteId ?: return
        // Unconfirmed changes of an existing note are dropped with it
        if (autoSave) saveNow()
        closed = true
        appScope.launch {
            saveMutex.withLock { notesRepository.moveToTrash(listOf(id), System.currentTimeMillis()) }
            uiMessages.show(UiMessage(R.plurals.moved_to_trash, count = 1, undo = { notesRepository.restore(listOf(id)) }))
        }
        onDone()
    }

    override fun onCleared() {
        super.onCleared()
        val id = noteId ?: return
        if (autoSave) saveNow()
        val last = note
        val removeIfBlank = autoSave && !closed
        appScope.launch {
            saveMutex.withLock {
                // A new note that was left empty is not kept
                if (removeIfBlank && last != null && last.isBlank) notesRepository.deleteForever(listOf(id))
            }
            noteLock.noteClosed(id)
        }
    }

    companion object {
        private const val SAVE_DELAY_MILLIS = 400L
        private const val SUGGESTIONS_LIMIT = 30

        // Saves of all editors go one after another, in the order they were made
        private val saveMutex = Mutex()
    }
}
