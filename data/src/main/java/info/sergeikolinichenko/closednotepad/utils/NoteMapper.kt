package info.sergeikolinichenko.closednotepad.utils

import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteItemDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteWithItems
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import javax.inject.Inject

class NoteMapper @Inject constructor() {

    fun mapDbModelToEntity(model: NoteWithItems) = Note(
        id = model.note.id,
        type = NoteType.entries.firstOrNull { it.name == model.note.type } ?: NoteType.TEXT,
        title = model.note.title,
        body = model.note.body,
        items = model.items.sortedBy { it.position }.map { item ->
            NoteItem(
                id = item.id,
                text = item.text,
                isChecked = item.isChecked,
                quantity = item.quantity,
                section = item.section,
                price = item.price
            )
        },
        colorIndex = model.note.colorIndex,
        isLocked = model.note.isLocked,
        isPinned = model.note.isPinned,
        createdAt = model.note.createdAt,
        updatedAt = model.note.updatedAt,
        removedAt = model.note.removedAt
    )

    fun mapEntityToDbModel(note: Note) = NoteDbModel(
        id = note.id,
        type = note.type.name,
        title = note.title,
        body = note.body,
        colorIndex = note.colorIndex,
        isLocked = note.isLocked,
        isPinned = note.isPinned,
        createdAt = note.createdAt,
        updatedAt = note.updatedAt,
        removedAt = note.removedAt
    )

    fun mapItemsToDbModels(note: Note) = note.items.mapIndexed { index, item ->
        NoteItemDbModel(
            id = item.id,
            noteId = note.id,
            position = index,
            text = item.text,
            isChecked = item.isChecked,
            quantity = item.quantity,
            section = item.section,
            price = item.price
        )
    }
}
