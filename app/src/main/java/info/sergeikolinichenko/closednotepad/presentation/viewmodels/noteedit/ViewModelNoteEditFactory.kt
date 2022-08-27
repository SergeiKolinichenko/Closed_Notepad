package info.sergeikolinichenko.closednotepad.presentation.viewmodels.noteedit

import android.app.Application
import android.app.backup.BackupManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase

class ViewModelNoteEditFactory(application: Application): ViewModelProvider.Factory {

    private val repository = NoteRepositoryImpl(application)
    private val preferencesRepository = PreferencesRepositoryImpl(application)

    private val backupManager = BackupManager(application)

    private val getNoteEntryUseCase = GetNoteUseCase(repository)
    private val addNoteEntryUseCase = AddNoteUseCase(repository)
    private val editNoteEntryUseCase = EditNoteUseCase(repository)

    private val getPrefColorIndex = GetPrefColorIndexUseCase(preferencesRepository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNoteEdit::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNoteEdit(
                getNoteEntryUseCase,
                addNoteEntryUseCase,
                editNoteEntryUseCase,
                getPrefColorIndex,
                backupManager
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }

    }
}