package info.sergeikolinichenko.closednotepad.dbmodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_entries")
data class NoteEntryDbModel (
    @PrimaryKey(autoGenerate = false)
    val timeStamp: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int,
    val isLocked: Boolean
        )