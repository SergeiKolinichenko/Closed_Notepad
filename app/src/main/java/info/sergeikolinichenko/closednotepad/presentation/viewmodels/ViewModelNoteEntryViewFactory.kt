package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteEntryUseCase

class ViewModelNoteEntryViewFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)

    private val getNoteEntryUseCase = GetNoteEntryUseCase(repository)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteEntryView::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteEntryView( getNoteEntryUseCase ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}