package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import info.sergeikolinichenko.closednotepad.security.KeystoreDeviceKey
import info.sergeikolinichenko.closednotepad.security.LockedNotesEncryption
import info.sergeikolinichenko.closednotepad.security.NotesVault
import info.sergeikolinichenko.closednotepad.utils.NoteMapper

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
                .addCallback(object : RoomDatabase.Callback() {
                    // Deleted and overwritten text is zeroed in the file, not left in free pages
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        db.query("PRAGMA secure_delete = ON").use { it.moveToFirst() }
                    }
                })
                .build()

        @Provides
        fun provideNotesDao(database: AppDatabase): NotesDao = database.notesDao()

        // One vault for the whole app: it holds the opened key while the app is in front
        @Provides
        @ApplicationScope
        fun provideNotesVault(application: Application): NotesVault = NotesVault(
            application.getSharedPreferences(NotesVault.PREFS_NAME, Context.MODE_PRIVATE),
            KeystoreDeviceKey(application)
        )

        @Provides
        @ApplicationScope
        fun provideLockedNotesEncryption(database: AppDatabase, vault: NotesVault): LockedNotesEncryption =
            LockedNotesEncryption(database, vault, NoteMapper())

        @Provides
        @ApplicationScope
        fun provideSharedPreferences(application: Application): SharedPreferences =
            application.getSharedPreferences(
                PreferencesRepositoryImpl.SHARED_PREFS_NAME,
                Context.MODE_PRIVATE
            )
    }
}
