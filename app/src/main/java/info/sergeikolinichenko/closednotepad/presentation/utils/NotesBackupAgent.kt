package info.sergeikolinichenko.closednotepad.presentation.utils

import android.app.backup.*
import android.os.ParcelFileDescriptor
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.preferences.SharedPrefNotes
import java.io.File
import java.io.IOException

class NotesBackupAgent: BackupAgentHelper() {

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesBackupHelper(this, SharedPrefNotes.SHARED_PREFS_NAME)
            .also { addHelper(PREFS_BACKUP_KEY, it) }

        FileBackupHelper(this, AppDatabase.DB_NAME)
            .also { addHelper(DB_BACKUP_KEY,it) }
    }

    @Throws(IOException::class)
    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) {
        synchronized(AppDatabase.LOCK) {
            super.onBackup(oldState, data, newState)
        }
    }

    @Throws(IOException::class)
    override fun onRestore(
        data: BackupDataInput?,
        appVersionCode: Long,
        newState: ParcelFileDescriptor?
    ) {
        synchronized(AppDatabase.LOCK){
            super.onRestore(data, appVersionCode, newState)
        }
    }

    override fun getDatabasePath(name: String?): File {
        return getDatabasePath(AppDatabase.DB_NAME)
    }

    companion object{
        private const val PREFS_BACKUP_KEY = "closed_notepad_prefs"
        private const val DB_BACKUP_KEY = "closed_notepad_db"

        fun requestBackup(backupManager: BackupManager) {
            backupManager.dataChanged()
        }
    }
}