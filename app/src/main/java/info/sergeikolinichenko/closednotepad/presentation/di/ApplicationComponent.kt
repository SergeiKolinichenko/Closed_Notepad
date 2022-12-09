package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import info.sergeikolinichenko.closednotepad.presentation.NotesApp
import info.sergeikolinichenko.closednotepad.presentation.screens.*

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

    fun inject(fragment: NotesMenuFragment)
    fun inject(fragment: NoteViewFragment)
    fun inject(fragment: TrashCanListFragment)
    fun inject(fragment: TrashCanViewFragment)
    fun inject(fragment: NoteEditFragment)
    fun inject(fragment: NoteListFragment)
    fun inject(fragment: NoteSearchFragment)

    fun inject(application: NotesApp)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ApplicationComponent
    }
}