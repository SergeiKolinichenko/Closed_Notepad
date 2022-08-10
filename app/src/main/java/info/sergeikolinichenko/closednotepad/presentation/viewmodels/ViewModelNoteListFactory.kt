package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefOrderNoteListUseCase

class ViewModelNoteListFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)
    private val preferencesRepository = PreferencesRepositoryImpl(application)

    private val getListNote = GetListNotesUseCase(repository)
    private val removeNote = RemoveNoteUseCase(repository)
    private val editNote = EditNoteUseCase(repository)
    private val addNote = AddNoteUseCase(repository)

    private val setPrefOrderNoteList = SetPrefOrderNoteListUseCase(preferencesRepository)
    private val getPrefOrderNoteList = GetPrefOrderNoteListUseCase(preferencesRepository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteList::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteList( getListNote, getPrefOrderNoteList,
                removeNote, editNote, setPrefOrderNoteList,
                addNote
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}