package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase

class ViewModelNoteEntryEditFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)

    private val getNoteEntryUseCase = GetNoteUseCase(repository)
    private val editNoteEntryUseCase = EditNoteUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteEntryEdit::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteEntryEdit( getNoteEntryUseCase, editNoteEntryUseCase ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }

    }
}