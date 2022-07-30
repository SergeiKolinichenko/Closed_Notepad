package info.sergeikolinichenko.closednotepad.dbmodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_entries")
data class RemovedNoteDbModel (
    @PrimaryKey(autoGenerate = false)
    val timeStamp: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int
        )