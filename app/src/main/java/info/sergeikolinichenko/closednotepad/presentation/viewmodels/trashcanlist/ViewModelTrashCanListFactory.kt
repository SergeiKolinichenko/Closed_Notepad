package info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanlist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetListRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.RecoveryRemovedNoteUseCase

class ViewModelTrashCanListFactory(application: Application): ViewModelProvider.Factory {

    private val repositoryRemovedNote = RemovedNoteRepositoryImpl(application)
    private val preferencesRepository = PreferencesRepositoryImpl(application)
    private val repositoryNote = NoteRepositoryImpl(application)

    private val getRemovedNoteList = GetListRemovedNoteUseCase(repositoryRemovedNote)
    private val deleteRemovedNote = DeleteRemovedNoteUseCase(repositoryRemovedNote)
    private val recoveryRemovedNote = RecoveryRemovedNoteUseCase(repositoryRemovedNote)
    private val addNote = AddNoteUseCase(repositoryNote)
    private val getPrefOrderNoteList = GetPrefOrderNoteListUseCase(preferencesRepository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelTrashCanList::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelTrashCanList(
                getRemovedNoteList,
                getPrefOrderNoteList,
                deleteRemovedNote,
                recoveryRemovedNote,
                addNote
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}