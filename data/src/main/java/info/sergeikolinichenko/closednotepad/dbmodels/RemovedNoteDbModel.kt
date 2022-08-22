package info.sergeikolinichenko.closednotepad.dbmodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "removed_notes")
data class RemovedNoteDbModel (
    @PrimaryKey(autoGenerate = false)
    val timeStamp: Long,
    val timeRemove: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int,
    val isLocked: Boolean
        )