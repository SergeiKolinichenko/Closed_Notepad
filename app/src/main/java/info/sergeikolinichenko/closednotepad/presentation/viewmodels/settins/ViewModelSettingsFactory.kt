package info.sergeikolinichenko.closednotepad.presentation.viewmodels.settins

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefColorIndexUseCase

class ViewModelSettingsFactory(application: Application): ViewModelProvider.Factory {

    private val repository = PreferencesRepositoryImpl(application)

    private val setPrefColorIndex = SetPrefColorIndexUseCase(repository)
    private val getPrefColorIndex = GetPrefColorIndexUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelSettings::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelSettings( setPrefColorIndex, getPrefColorIndex ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}