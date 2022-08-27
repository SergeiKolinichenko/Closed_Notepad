package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notelist

import android.app.Application
import android.app.backup.BackupManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetListRemovedNoteUseCase

class ViewModelNoteListFactory(application: Application): ViewModelProvider.Factory {

    private val repositoryNotes = NoteRepositoryImpl(application)
    private val repositoryRemovedNotes = RemovedNoteRepositoryImpl(application)
    private val preferencesRepository = PreferencesRepositoryImpl(application)

    private val getListNote = GetListNotesUseCase(repositoryNotes)
    private val removeNote = RemoveNoteUseCase(repositoryNotes)
    private val editNote = EditNoteUseCase(repositoryNotes)

    private val getRemovedNoteList = GetListRemovedNoteUseCase(repositoryRemovedNotes)
    private val addRemovedNote = AddRemovedNoteUseCase(repositoryRemovedNotes)
    private val deleteRemovedNote = DeleteRemovedNoteUseCase(repositoryRemovedNotes)

    private val setPrefOrderNoteList = SetPrefOrderNoteListUseCase(preferencesRepository)
    private val getPrefOrderNoteList = GetPrefOrderNoteListUseCase(preferencesRepository)
    private val getPrefDayBeforeDelete = GetPrefAutoDelReNoteUseCase(preferencesRepository)

    private val backupManager = BackupManager(application)

    private val addNote = AddNoteUseCase(repositoryNotes) // TODO delete later

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteList::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteList(
                getListNote, removeNote, editNote,
                getRemovedNoteList, addRemovedNote, deleteRemovedNote,
                getPrefOrderNoteList, setPrefOrderNoteList, getPrefDayBeforeDelete,
                backupManager,
                addNote
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}