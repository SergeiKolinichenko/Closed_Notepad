package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.backup.BackupManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.presentation.utils.NotesBackupAgent
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetListRemovedNoteUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewModelNoteList @Inject constructor(
    getListNote: GetListNotesUseCase,
    private val removeNote: RemoveNoteUseCase,
    private val editNote: EditNoteUseCase,
    private val getRemovedNoteList: GetListRemovedNoteUseCase,
    private val addRemovedNote: AddRemovedNoteUseCase,
    private val deleteRemovedNote: DeleteRemovedNoteUseCase,
    getPrefOrderListNote: GetPrefOrderNoteListUseCase,
    private val setPrefOrderListNote: SetPrefOrderNoteListUseCase,
    private val getPrefDayBeforeDelete: GetPrefAutoDelReNoteUseCase,
    private val backupManager: BackupManager
) : ViewModel() {

    private var _noteList: MutableLiveData<List<Note>> = getListNote.invoke()
    val noteList: LiveData<List<Note>>
        get() = _noteList

    private var _removedNoteList = MutableLiveData<List<RemovedNote>>()
    val removedNoteList: LiveData<List<RemovedNote>>
        get() = _removedNoteList

    private var _orderViewNoteList: String? = getPrefOrderListNote.invoke()
    val orderViewNoteList: String
        get() = _orderViewNoteList ?: throw RuntimeException("orderViewNoteList equal null")

    private val _isSelected = MutableLiveData(false)
    val isSelected: LiveData<Boolean>
        get() = _isSelected

    private var _showColorButtons = MutableLiveData<Boolean>()
    val showColorButtons: LiveData<Boolean>
        get() = _showColorButtons

    private var _showOrderButtons = MutableLiveData<Boolean>()
    val showOrderButtons: LiveData<Boolean>
        get() = _showOrderButtons

    private val selectedNotes = mutableListOf<Note>()

    init {
        initAutoDeleteRemovedNote()
    }

    fun selectNotes(note: Note) {
        if (isSelected.value == false || isSelected.value == null) _isSelected.value = true

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
            _noteList.value = notes
        }
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
        for (item in selectedNotes) {
            val newItem = item.copy(colorIndex = colorIndex, isSelected = !item.isSelected)
            viewModelScope.launch {
                editNote.invoke(newItem)
            }
        }
        clearSelectedNotes()
        NotesBackupAgent.requestBackup(backupManager)
        _showColorButtons.value = false
    }

    private fun clearSelectedNotes() {
        selectedNotes.clear()
        _isSelected.value = false
    }

    fun setOrderViewNoteList(order: String) {
        _orderViewNoteList = order
        setPrefOrderListNote.invoke(order)
        _noteList.value = noteList.value
        _showOrderButtons.value = false
        NotesBackupAgent.requestBackup(backupManager)
    }

    fun setStateShowColorButtons() {
        _showColorButtons.value =
            showColorButtons.value == null || showColorButtons.value == false
    }

    fun setStateShowOrderButtons() {
        _showOrderButtons.value =
            showOrderButtons.value == null || showOrderButtons.value == false
    }

    private fun initAutoDeleteRemovedNote() {
        val days = getPrefDayBeforeDelete.invoke()
        if (days > 0) {
            _removedNoteList = getRemovedNoteList.invoke()
        }
    }

    fun autoDeleteRemovedNote() {
        val days = getPrefDayBeforeDelete.invoke()
        val list = removedNoteList.value
        list?.let {
            for (item in list) {
                if (TimeUtils.getDiffDays(item.timeStamp) > days) {
                    viewModelScope.launch {
                        deleteRemovedNote.invoke(item.timeStamp)
                    }
                }
            }
            NotesBackupAgent.requestBackup(backupManager)
        }
        _removedNoteList.value = null
    }

}