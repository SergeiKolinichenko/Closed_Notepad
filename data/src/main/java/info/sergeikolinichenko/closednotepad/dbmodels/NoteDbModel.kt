package info.sergeikolinichenko.closednotepad.dbmodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteDbModel (
    @PrimaryKey(autoGenerate = false)
    val timeStamp: Long,
    val titleNote: String,
    val itselfNote: String,
    val colorIndex: Int,
    val isLocked: Boolean
        )