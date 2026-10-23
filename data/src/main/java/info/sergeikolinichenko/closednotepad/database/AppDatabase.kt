package info.sergeikolinichenko.closednotepad.database

import androidx.room.Database
import androidx.room.RoomDatabase
import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteItemDbModel

@Database(
    entities = [NoteDbModel::class, NoteItemDbModel::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao

    companion object {
        const val DB_NAME = "note_database.db"
    }
}
