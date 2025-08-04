package info.sergeikolinichenko.closednotepad.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.RemovedNoteDbModel

@Database(
    entities = [NoteDbModel::class, RemovedNoteDbModel::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun notesDao(): NotesDao

    companion object{

        private var INSTANCE: AppDatabase? = null
        val LOCK = Any()
        const val DB_NAME = "note_database.db"

        fun getInstance(application: Application): AppDatabase {
            INSTANCE?.let {
                return it
            }
            synchronized(LOCK) {
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(
                    application,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .build()
                INSTANCE = db
                return db
            }
        }
    }

}