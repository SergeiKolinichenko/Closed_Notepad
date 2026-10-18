package info.sergeikolinichenko.closednotepad.dbmodels

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithItems(
    @Embedded
    val note: NoteDbModel,
    @Relation(parentColumn = "id", entityColumn = "noteId")
    val items: List<NoteItemDbModel>
)
