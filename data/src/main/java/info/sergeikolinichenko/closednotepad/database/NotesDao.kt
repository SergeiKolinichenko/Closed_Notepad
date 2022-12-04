package info.sergeikolinichenko.closednotepad.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.RemovedNoteDbModel

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes")
    fun getNoteList():LiveData<List<NoteDbModel>>

    @Query("SELECT * FROM notes WHERE timeStamp = :timeStamp LIMIT 1")
    suspend fun getNote(timeStamp: Long): NoteDbModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(noteDbModel: NoteDbModel)

    @Query("DELETE FROM notes WHERE timeStamp = :timeStamp")
    suspend fun deleteNote(timeStamp: Long)

    @Query("SELECT * FROM removed_notes")
    fun getRemovedNoteListLiveData():LiveData<List<RemovedNoteDbModel>>

    @Query("SELECT * FROM removed_notes")
    fun getRemovedNoteList():List<RemovedNoteDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRemovedNote(removedNoteDbModel: RemovedNoteDbModel)

    @Query("SELECT * FROM removed_notes WHERE timeStamp = :timeStamp LIMIT 1")
    suspend fun getRemovedNote(timeStamp: Long): RemovedNoteDbModel

    @Query("DELETE FROM removed_notes WHERE timeStamp = :timeStamp")
    suspend fun deleteRemovedNote(timeStamp: Long)
}