package info.sergeikolinichenko.closednotepad.database

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.sergeikolinichenko.closednotepad.dbmodels.NoteEntryDbModel

interface NoteEntriesDao {

    @Query("SELECT * FROM note_entries")
    fun getNoteList(): List<NoteEntryDbModel>

    @Query("SELECT * FROM note_entries WHERE timeStamp = :timeStamp LIMIT 1")
    fun getNoteEntry(timeStamp: Long): NoteEntryDbModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNoteEntry(noteEntryDbModel: NoteEntryDbModel)

    @Query("DELETE FROM note_entries WHERE timeStamp = :timeStamp")
    fun deleteNoteEntry(timeStamp: Long)
}