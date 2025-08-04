package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.backup.BackupManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.presentation.utils.NotesBackupAgent
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetListRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.RecoveryRemovedNoteUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewModelTrashCanList @Inject constructor(
    getRemovedNoteList: GetListRemovedNoteUseCase,
    getPrefOrderNoteList: GetPrefOrderNoteListUseCase,
    private val deleteRemovedNote: DeleteRemovedNoteUseCase,
    private val recoveryRemovedNote: RecoveryRemovedNoteUseCase,
    private val addNote: AddNoteUseCase,
    private val backupManager: BackupManager
) : ViewModel() {

    private var _removedNoteList: MutableLiveData<List<RemovedNote>> = getRemovedNoteList.invoke()
    val removedNoteList: LiveData<List<RemovedNote>>
        get() = _removedNoteList

    private val selectedRemovedNotes = mutableListOf<RemovedNote>()

    private var _orderViewTrashCanList: String? = getPrefOrderNoteList.invoke()
    val orderViewTrashCanList: String
        get() = _orderViewTrashCanList ?: throw RuntimeException("orderViewNoteList equal null")

    private val _isSelected = MutableLiveData(false)
    val isSelected: LiveData<Boolean>
        get() = _isSelected

    fun selectRemovedNotes(removedNote: RemovedNote) {
        if (isSelected.value == false) {
            _isSelected.value = true
        }

        if (selectedRemovedNotes.contains(removedNote)) {
            reselectRemovedNotes()
        } else {
            val removedNotes: MutableList<RemovedNote> = removedNoteList.value?.toMutableList()
                ?: throw RuntimeException("removedNoteList equal null")
            val newItem = removedNote.copy(isSelected = !removedNote.isSelected)
            selectedRemovedNotes.add(newItem)
            removedNotes.remove(removedNote)
            removedNotes.add(newItem)
            _removedNoteList.value = removedNotes
        }
    }

    fun reselectRemovedNotes() {
        val removedNotes: MutableList<RemovedNote> = removedNoteList.value?.toMutableList()
            ?: throw RuntimeException("removedNoteList equal null")

        for (item in selectedRemovedNotes) {
            val newItem = item.copy(isSelected = !item.isSelected)
            removedNotes.remove(item)
            removedNotes.add(newItem)
            _removedNoteList.value = removedNotes
        }
        clearSelectedRemovedNotes()
    }

    private fun clearSelectedRemovedNotes() {
        selectedRemovedNotes.clear()
        _isSelected.value = false
    }

    fun recoveryRemovedNotes() {
        for (item in selectedRemovedNotes) {
            viewModelScope.launch {
                addNote.invoke(recoveryRemovedNote.invoke(item))
            }
        }
        NotesBackupAgent.requestBackup(backupManager)
        clearSelectedRemovedNotes()
    }

    fun deleteRemovedNotes() {
        for (item in selectedRemovedNotes) {
            viewModelScope.launch {
                deleteRemovedNote.invoke(item.timeStamp)
            }
        }
        NotesBackupAgent.requestBackup(backupManager)
        clearSelectedRemovedNotes()
    }

}