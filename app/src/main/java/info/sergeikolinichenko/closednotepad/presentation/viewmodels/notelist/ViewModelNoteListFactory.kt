package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notelist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase

class ViewModelNoteListFactory(application: Application): ViewModelProvider.Factory {

    private val repoNotes = NoteRepositoryImpl(application)
    private val repoRemovedNotes = RemovedNoteRepositoryImpl(application)
    private val preferencesRepository = PreferencesRepositoryImpl(application)

    private val getListNote = GetListNotesUseCase(repoNotes)
    private val removeNote = RemoveNoteUseCase(repoNotes)
    private val editNote = EditNoteUseCase(repoNotes)
    private val addRemovedNote = AddRemovedNoteUseCase(repoRemovedNotes)

    private val addNote = AddNoteUseCase(repoNotes)

    private val setPrefOrderNoteList = SetPrefOrderNoteListUseCase(preferencesRepository)
    private val getPrefOrderNoteList = GetPrefOrderNoteListUseCase(preferencesRepository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteList::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteList( getListNote, getPrefOrderNoteList,
                removeNote, editNote, setPrefOrderNoteList,
                addRemovedNote,
                addNote
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}