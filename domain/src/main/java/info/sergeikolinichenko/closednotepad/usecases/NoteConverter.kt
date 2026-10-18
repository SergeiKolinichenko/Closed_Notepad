package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType

object NoteConverter {

    const val UNCHECKED_MARK = "☐ "
    const val CHECKED_MARK = "☑ "

    private val bulletPrefixes = listOf("- [ ] ", "- [x] ", "- [X] ", "☐ ", "☑ ", "• ", "- ", "* ")

    // Every non-empty line of the text becomes a list item
    fun textToList(note: Note, newId: () -> String): Note {
        val items = note.body.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
                val prefix = bulletPrefixes.firstOrNull { line.startsWith(it) }
                val checked = prefix == CHECKED_MARK || prefix == "- [x] " || prefix == "- [X] "
                NoteItem(
                    id = newId(),
                    text = if (prefix != null) line.removePrefix(prefix).trim() else line,
                    isChecked = checked
                )
            }
        return note.copy(type = NoteType.CHECKLIST, body = "", items = items)
    }

    // Every item becomes a line with a check mark
    fun listToText(note: Note): Note {
        val body = note.items
            .filter { it.text.isNotBlank() }
            .joinToString("\n") { item ->
                val mark = if (item.isChecked) CHECKED_MARK else UNCHECKED_MARK
                val quantity = if (item.quantity.isNotBlank()) " — ${item.quantity.trim()}" else ""
                mark + item.text.trim() + quantity
            }
        return note.copy(type = NoteType.TEXT, body = body, items = emptyList())
    }
}
