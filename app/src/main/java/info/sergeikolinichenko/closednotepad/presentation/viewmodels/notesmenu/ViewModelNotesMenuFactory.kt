package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesmenu

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefColorIndexUseCase

class ViewModelNotesMenuFactory(application: Application): ViewModelProvider.Factory {

    private val repository = PreferencesRepositoryImpl(application)

    private val setPrefColorIndex = SetPrefColorIndexUseCase(repository)
    private val getPrefColorIndex = GetPrefColorIndexUseCase(repository)
    private val setPrefAutoDelReNote = SetPrefAutoDelReNoteUseCase(repository)
    private val getPrefAutoDelReNote = GetPrefAutoDelReNoteUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelNotesMenu::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelNotesMenu(
                setPrefColorIndex,
                getPrefColorIndex,
                setPrefAutoDelReNote,
                getPrefAutoDelReNote
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}