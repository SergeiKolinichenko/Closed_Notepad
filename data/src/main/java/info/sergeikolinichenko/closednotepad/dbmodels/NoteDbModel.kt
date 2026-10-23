package info.sergeikolinichenko.closednotepad.dbmodels

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [Index("removedAt")]
)
data class NoteDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: String,
    val title: String,
    val body: String,
    val colorIndex: Int,
    val isLocked: Boolean,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val removedAt: Long?
)
