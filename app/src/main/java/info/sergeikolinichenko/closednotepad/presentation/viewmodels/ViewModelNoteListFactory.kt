package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase

class ViewModelNoteListFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)

    private val getListNoteUseCase = GetListNotesUseCase(repository)
    private val removeEntryFromNoteUseCase = RemoveNoteUseCase(repository)
    private val editEntryAtNoteUseCase = EditNoteUseCase(repository)
    private val addEntryToNoteUseCase = AddNoteUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteList::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteList(
                getListNoteUseCase,
                removeEntryFromNoteUseCase,
                editEntryAtNoteUseCase,
                addEntryToNoteUseCase
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}