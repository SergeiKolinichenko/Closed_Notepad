package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase

class ViewModelNoteEditFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)

    private val getNoteEntryUseCase = GetNoteUseCase(repository)
    private val addNoteEntryUseCase = AddNoteUseCase(repository)
    private val editNoteEntryUseCase = EditNoteUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteEdit::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteEdit(
                getNoteEntryUseCase,
                addNoteEntryUseCase,
                editNoteEntryUseCase ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }

    }
}