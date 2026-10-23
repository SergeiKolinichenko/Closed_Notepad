package info.sergeikolinichenko.closednotepad.models

enum class NoteType { TEXT, CHECKLIST, SHOPPING }

data class NoteItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false,
    val quantity: String = "",
    val section: String = "",
    val price: Double? = null
)

data class Note(
    val id: Long = 0,
    val type: NoteType,
    val title: String = "",
    val body: String = "",
    val items: List<NoteItem> = emptyList(),
    val colorIndex: Int,
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val removedAt: Long? = null,
    // A locked note whose content is still encrypted: body and items are empty until it is unlocked
    val isSealed: Boolean = false
) {
    // A sealed note is never blank, its content is just not readable yet
    val isBlank: Boolean
        get() = !isSealed && title.isBlank() && body.isBlank() && items.all { it.text.isBlank() }

    val checkedCount: Int
        get() = items.count { it.isChecked }

    val isInTrash: Boolean
        get() = removedAt != null

    companion object {
        // An empty note to start writing in; lists get one empty item to type into
        fun blank(type: NoteType, colorIndex: Int, now: Long, newItemId: () -> String): Note = Note(
            type = type,
            colorIndex = colorIndex,
            items = if (type == NoteType.TEXT) emptyList() else listOf(NoteItem(id = newItemId(), text = "")),
            createdAt = now,
            updatedAt = now
        )
    }
}
