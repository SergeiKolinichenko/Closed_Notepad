package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesearch

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase

class ViewModelNoteSearchFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)

    private val getNoteList = GetListNotesUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteSearch::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteSearch( getNoteList ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}