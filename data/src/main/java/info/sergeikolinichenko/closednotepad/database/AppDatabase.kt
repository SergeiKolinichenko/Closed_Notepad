package info.sergeikolinichenko.closednotepad.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.sergeikolinichenko.closednotepad.dbmodels.NoteEntryDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.TrashEntryDbModel

@Database(entities = [NoteEntryDbModel::class,
    TrashEntryDbModel::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun noteEntriesDao(): NoteEntriesDao

    companion object{

        private var INSTANCE: AppDatabase? = null
        private val LOCK = Any()
        private const val DB_NAME = "note_database.db"

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
                    .allowMainThreadQueries() // TODO delete string after debugging
                    .build()
                INSTANCE = db
                return db
            }
        }
    }

}