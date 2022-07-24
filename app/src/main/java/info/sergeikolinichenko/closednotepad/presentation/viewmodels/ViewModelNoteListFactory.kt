package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddEntryToNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveEntryFromNoteUseCase

class ViewModelNoteListFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)

    private val getListNoteUseCase = GetListNoteUseCase(repository)
    private val removeEntryFromNoteUseCase = RemoveEntryFromNoteUseCase(repository)
    private val addEntryToNoteUseCase = AddEntryToNoteUseCase(repository)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteList::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteList(
                getListNoteUseCase, removeEntryFromNoteUseCase, addEntryToNoteUseCase
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}