package info.sergeikolinichenko.closednotepad.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes")
    suspend fun getNoteList(): List<NoteDbModel>

    @Query("SELECT * FROM notes WHERE timeStamp = :timeStamp LIMIT 1")
    suspend fun getNoteEntry(timeStamp: Long): NoteDbModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNoteEntry(noteDbModel: NoteDbModel)

    @Query("DELETE FROM notes WHERE timeStamp = :timeStamp")
    suspend fun deleteNoteEntry(timeStamp: Long)
}