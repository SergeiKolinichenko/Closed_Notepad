package info.sergeikolinichenko.closednotepad.presentation.viewmodels.noteview

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase

class ViewModelNoteViewFactory(application: Application) : ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)
    private val repositoryRemovedNotes = RemovedNoteRepositoryImpl(application)

    private val getNoteUseCase = GetNoteUseCase(repository)
    private val removeNoteUseCase = RemoveNoteUseCase(repository)
    private val addRemovedNote = AddRemovedNoteUseCase(repositoryRemovedNotes)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ViewModelNoteView::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteView(
                getNoteUseCase,
                removeNoteUseCase,
                addRemovedNote
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}