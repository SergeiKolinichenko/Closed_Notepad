package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.database.NotesDao
import info.sergeikolinichenko.closednotepad.preferences.SharedPrefNotes
import info.sergeikolinichenko.closednotepad.repositories.*

/** Created by Sergei Kolinichenko on 25.11.2022 at 19:10 (GMT+3) **/

@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindNoteRepository(impl: NoteRepositoryImpl): NotesRepository

    @Binds
    @ApplicationScope
    fun bindRemovedNoteRepository(impl: RemovedNoteRepositoryImpl): RemovedNoteRepository

    @Binds
    @ApplicationScope
    fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    companion object{

        @Provides
        fun provideNotesDao(application: Application): NotesDao {
            return AppDatabase.getInstance(application).notesDao()
        }

        @Provides
        fun provideSharedPrefNotes(application: Application): SharedPreferences {
            return SharedPrefNotes.getInstance(application)
        }
    }
}