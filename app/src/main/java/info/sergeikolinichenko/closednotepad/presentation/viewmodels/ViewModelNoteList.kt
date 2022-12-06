package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.backup.BackupManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.stateful.*
import info.sergeikolinichenko.closednotepad.presentation.utils.NotesBackupAgent
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemoveNotesUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewModelNoteList @Inject constructor(
    getListNote: GetListNotesUseCase,
    private val removeNote: RemoveNoteUseCase,
    private val editNote: EditNoteUseCase,
    private val addRemovedNote: AddRemovedNoteUseCase,
    getPrefOrderListNote: GetPrefOrderNoteListUseCase,
    private val setPrefOrderListNote: SetPrefOrderNoteListUseCase,
    private val backupManager: BackupManager,
    deleteRemovedNotes: DeleteRemoveNotesUseCase
) : ViewModel() {

    private var _noteList: MutableLiveData<List<Note>> = getListNote.invoke()
    val noteList: LiveData<List<Note>>
        get() = _noteList

    private var _orderViewNoteList: String? = getPrefOrderListNote.invoke()
    val orderViewNoteList: String
        get() = _orderViewNoteList ?: throw RuntimeException("orderViewNoteList equal null")

    private val _stateNoteList = MutableLiveData<StateNoteList>(ItemUnselected)
    val stateNoteList: LiveData<StateNoteList>
        get() = _stateNoteList

    private val selectedNotes = mutableListOf<Note>()

    init {
        deleteRemovedNotes.invoke()
    }

    fun selectNotes(note: Note) {
        if (selectedNotes.isEmpty()) _stateNoteList.value = ItemSelected

        if (selectedNotes.contains(note)) {
            resetSelectedNotes()
        } else {
            val notes: MutableList<Note> =
                noteList.value?.toMutableList() ?: throw RuntimeException("noteList equals null")

            val newItem = note.copy(isSelected = !note.isSelected)
            selectedNotes.add(newItem)
            notes.remove(note)
            notes.add(newItem)
            _noteList.value = notes
        }
    }

    fun resetSelectedNotes() {

        val notes: MutableList<Note> =
            noteList.value?.toMutableList() ?: throw RuntimeException("noteList equals null")

        for (item in selectedNotes) {
            val newItem = item.copy(isSelected = !item.isSelected)
            notes.remove(item)
            notes.add(newItem)
        }

        _noteList.value = notes
        clearSelectedNotes()
    }

    fun removeNotes() {
        for (item in selectedNotes) {
            viewModelScope.launch {
                addRemovedNote.invoke(removeNote.invoke(item))
            }
        }
        NotesBackupAgent.requestBackup(backupManager)
        clearSelectedNotes()
    }

    fun setColorIndex(colorIndex: Int) {
        _stateNoteList.value = HideColorButtonsNoteList
        for (item in selectedNotes) {
            val newItem = item.copy(colorIndex = colorIndex)
            viewModelScope.launch {
                editNote.invoke(newItem)
            }
        }
        resetSelectedNotes()
        clearSelectedNotes()
        NotesBackupAgent.requestBackup(backupManager)
    }

    private fun clearSelectedNotes() {
        selectedNotes.clear()
        _stateNoteList.value = ItemUnselected
    }

    fun setOrderViewNoteList(order: String) {
        _orderViewNoteList = order
        setPrefOrderListNote.invoke(order)
        _noteList.value = noteList.value
        _stateNoteList.value = HideOrderButtons
        NotesBackupAgent.requestBackup(backupManager)
    }

    fun showColorButtons() {
        _stateNoteList.value = ShowColorButtonsNoteList
    }

    fun hideColorButtons() {
        _stateNoteList.value = HideColorButtonsNoteList
    }

    fun showOrderButtons() {
        _stateNoteList.value = ShowOrderButtons
    }

    fun hideOrderButtons() {
        _stateNoteList.value = HideOrderButtons
    }

}