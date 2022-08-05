package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase

class ViewModelNoteViewFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)

    private val getNoteUseCase = GetNoteUseCase(repository)
    private val removeNoteUseCase = RemoveNoteUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteView::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteView( getNoteUseCase, removeNoteUseCase ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}