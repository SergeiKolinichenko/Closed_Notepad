package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.backup.BackupManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.presentation.utils.NotesBackupAgent
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.RecoveryRemovedNoteUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewModelTrashCanView @Inject constructor(
    private val getRemovedNote: GetRemovedNoteUseCase,
    private val recoveryRemovedNote: RecoveryRemovedNoteUseCase,
    private val deleteRemovedNote: DeleteRemovedNoteUseCase,
    private val addNote: AddNoteUseCase,
    private val backupManager: BackupManager
) : ViewModel() {

    private var _removedNote = MutableLiveData<RemovedNote>()
    val removedNote: LiveData<RemovedNote>
        get() = _removedNote

    private var _endUsingFragment = MutableLiveData<Unit>()
    val endUsingFragment: LiveData<Unit>
        get() = _endUsingFragment

    fun getRemovedNote(timeStamp: Long) {
        viewModelScope.launch {
            _removedNote.value = getRemovedNote.invoke(timeStamp)
        }
    }

    fun recoveryRemovedNote() {
        val removedNote: RemovedNote =
            removedNote.value ?: throw RuntimeException("removedNote equal null")

        viewModelScope.launch {
            addNote.invoke(recoveryRemovedNote.invoke(removedNote))
        }
        NotesBackupAgent.requestBackup(backupManager)
        _endUsingFragment.value = Unit
    }

    fun deleteRemovedNote() {
        val timeStamp: Long =
            removedNote.value?.timeStamp ?: throw RuntimeException("timeStamp equal null")

        viewModelScope.launch {
            deleteRemovedNote.invoke(timeStamp)
        }
        NotesBackupAgent.requestBackup(backupManager)
        _endUsingFragment.value = Unit
    }

    fun endUsingFragment() {
        _endUsingFragment.value = Unit
    }

}