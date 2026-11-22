package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import info.sergeikolinichenko.closednotepad.presentation.NotesApp
import info.sergeikolinichenko.closednotepad.presentation.security.NoteLock
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessages
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNotesFactory
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import info.sergeikolinichenko.closednotepad.security.NotesVault

/** Created by Sergei Kolinichenko on 25.11.2022 at 19:05 (GMT+3) **/

@ApplicationScope
@Component(
    modules = [
        AppModule::class,
        DataModule::class,
        ViewModelsModule::class,
        WorkerModule::class
    ]
)
interface ApplicationComponent {

    fun viewModelFactory(): ViewModelNotesFactory
    fun noteLock(): NoteLock
    fun uiMessages(): UiMessages
    fun notesRepository(): NotesRepository
    fun preferencesRepository(): PreferencesRepository
    fun notesVault(): NotesVault

    fun inject(application: NotesApp)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ApplicationComponent
    }
}
