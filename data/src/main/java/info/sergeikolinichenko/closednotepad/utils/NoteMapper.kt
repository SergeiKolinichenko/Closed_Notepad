package info.sergeikolinichenko.closednotepad.utils

import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteItemDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteWithItems
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.security.NoteCipher
import info.sergeikolinichenko.closednotepad.security.SealedContent
import javax.inject.Inject

class NoteMapper @Inject constructor() {

    // open turns the encrypted content of a locked note back into JSON, or gives null while it is locked
    fun mapDbModelToEntity(model: NoteWithItems, open: (String) -> String? = { null }): Note {
        val sealed = NoteCipher.isSealed(model.note.body)
        val content = if (sealed) {
            open(model.note.body)?.let { json -> runCatching { SealedContent.fromJson(json) }.getOrNull() }
        } else {
            SealedContent(model.note.body, mapItems(model.items))
        }
        return mapDbModelToEntity(model, content)
    }

    private fun mapDbModelToEntity(model: NoteWithItems, content: SealedContent?) = Note(
        id = model.note.id,
        type = NoteType.entries.firstOrNull { it.name == model.note.type } ?: NoteType.TEXT,
        title = model.note.title,
        body = content?.body.orEmpty(),
        items = content?.items.orEmpty(),
        isSealed = content == null,
        colorIndex = model.note.colorIndex,
        isLocked = model.note.isLocked,
        isPinned = model.note.isPinned,
        createdAt = model.note.createdAt,
        updatedAt = model.note.updatedAt,
        removedAt = model.note.removedAt
    )

    private fun mapItems(items: List<NoteItemDbModel>) = items.sortedBy { it.position }.map { item ->
        NoteItem(
            id = item.id,
            text = item.text,
            isChecked = item.isChecked,
            quantity = item.quantity,
            section = item.section,
            price = item.price
        )
    }

    // The JSON that is encrypted in place of the body and the items of a locked note
    fun sealedContentJson(note: Note): String = SealedContent(note.body, note.items).toJson()

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
