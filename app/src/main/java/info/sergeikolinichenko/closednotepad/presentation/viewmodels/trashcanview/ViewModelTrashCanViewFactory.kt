package info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanview

import android.app.Application
import android.app.backup.BackupManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.RecoveryRemovedNoteUseCase

class ViewModelTrashCanViewFactory(application: Application): ViewModelProvider.Factory {

    private val repositoryRemovedNote = RemovedNoteRepositoryImpl(application)
    private val repositoryNote = NoteRepositoryImpl(application)

    private val backupManager = BackupManager(application)

    private val getRemovedNote = GetRemovedNoteUseCase(repositoryRemovedNote)
    private val deleteRemovedNote = DeleteRemovedNoteUseCase(repositoryRemovedNote)
    private val recoveryRemovedNote = RecoveryRemovedNoteUseCase(repositoryRemovedNote)
    private val addNote = AddNoteUseCase(repositoryNote)

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if(modelClass.isAssignableFrom(ViewModelTrashCanView::class.java)) {
                @Suppress("UNCHECKED_CAST")
                ViewModelTrashCanView( getRemovedNote,
                    recoveryRemovedNote,
                    deleteRemovedNote,
                    addNote,
                    backupManager
                ) as T
            } else {
                throw RuntimeException("Unknown view Model class $modelClass")
            }
    }
}