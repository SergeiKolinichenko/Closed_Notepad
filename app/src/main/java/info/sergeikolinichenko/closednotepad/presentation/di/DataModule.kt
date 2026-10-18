package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.database.MIGRATION_1_2
import info.sergeikolinichenko.closednotepad.database.NotesDao
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.NotesRepositoryImpl
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl

/** Created by Sergei Kolinichenko on 25.11.2022 at 19:10 (GMT+3) **/

@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindNotesRepository(impl: NotesRepositoryImpl): NotesRepository

    @Binds
    @ApplicationScope
    fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    companion object {

        @Provides
        @ApplicationScope
        fun provideDatabase(application: Application): AppDatabase =
            Room.databaseBuilder(application, AppDatabase::class.java, AppDatabase.DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()

        @Provides
        fun provideNotesDao(database: AppDatabase): NotesDao = database.notesDao()

        @Provides
        @ApplicationScope
        fun provideSharedPreferences(application: Application): SharedPreferences =
            application.getSharedPreferences(
                PreferencesRepositoryImpl.SHARED_PREFS_NAME,
                Context.MODE_PRIVATE
            )
    }
}
