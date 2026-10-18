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
    val removedAt: Long? = null
) {
    val isBlank: Boolean
        get() = title.isBlank() && body.isBlank() && items.all { it.text.isBlank() }

    val checkedCount: Int
        get() = items.count { it.isChecked }

    val isInTrash: Boolean
        get() = removedAt != null
}
