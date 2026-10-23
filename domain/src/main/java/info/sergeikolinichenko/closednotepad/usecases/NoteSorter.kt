package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.SortOrder

object NoteSorter {

    // Pinned notes always go first
    fun sort(notes: List<Note>, order: SortOrder): List<Note> {
        val byOrder: Comparator<Note> = when (order) {
            SortOrder.UPDATED_DESC -> compareByDescending { it.updatedAt }
            SortOrder.CREATED_DESC -> compareByDescending { it.createdAt }
            SortOrder.CREATED_ASC -> compareBy { it.createdAt }
            SortOrder.TITLE_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { NoteText.displayTitle(it) }
            SortOrder.TITLE_DESC ->
                compareByDescending(String.CASE_INSENSITIVE_ORDER) { NoteText.displayTitle(it) }
            SortOrder.COLOR -> compareBy<Note> { it.colorIndex }.thenByDescending { it.updatedAt }
        }
        return notes.sortedWith(compareByDescending<Note> { it.isPinned }.then(byOrder))
    }
}
