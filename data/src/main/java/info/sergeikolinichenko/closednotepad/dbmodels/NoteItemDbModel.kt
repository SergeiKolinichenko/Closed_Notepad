package info.sergeikolinichenko.closednotepad.dbmodels

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_items",
    foreignKeys = [
        ForeignKey(
            entity = NoteDbModel::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class NoteItemDbModel(
    @PrimaryKey
    val id: String,
    val noteId: Long,
    val position: Int,
    val text: String,
    val isChecked: Boolean,
    val quantity: String,
    val section: String,
    val price: Double?
)
