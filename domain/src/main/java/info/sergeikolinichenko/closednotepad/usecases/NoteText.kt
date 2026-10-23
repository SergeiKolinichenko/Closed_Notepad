package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType

object NoteText {

    const val MAX_DERIVED_TITLE_LENGTH = 40

    // Title shown for the note: its own title, or one made from the first line of the content
    fun displayTitle(note: Note): String {
        if (note.title.isNotBlank()) return note.title.trim()
        val source = when (note.type) {
            NoteType.TEXT -> note.body
            NoteType.CHECKLIST, NoteType.SHOPPING ->
                note.items.firstOrNull { it.text.isNotBlank() }?.text.orEmpty()
        }
        return deriveTitle(source)
    }

    // Takes the first non-empty line and shortens it on a word boundary
    fun deriveTitle(text: String, maxLength: Int = MAX_DERIVED_TITLE_LENGTH): String {
        val line = text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return ""
        if (line.length <= maxLength) return line.capitalizeFirst()
        val cut = line.substring(0, maxLength)
        val lastSpace = cut.lastIndexOf(' ')
        val short = if (lastSpace > maxLength / 2) cut.substring(0, lastSpace) else cut
        return short.trimEnd(' ', ',', '.', ';', ':', '-').capitalizeFirst() + "…"
    }

    // Plain text used for sharing and copying
    fun toPlainText(note: Note): String = buildString {
        val title = note.title.trim()
        if (title.isNotEmpty()) appendLine(title)
        when (note.type) {
            NoteType.TEXT -> {
                if (title.isNotEmpty() && note.body.isNotBlank()) appendLine()
                append(note.body.trim())
            }
            NoteType.CHECKLIST, NoteType.SHOPPING -> {
                if (title.isNotEmpty()) appendLine()
                note.items.filter { it.text.isNotBlank() }.forEach { item ->
                    append(if (item.isChecked) NoteConverter.CHECKED_MARK else NoteConverter.UNCHECKED_MARK)
                    append(item.text.trim())
                    if (item.quantity.isNotBlank()) append(" — ").append(item.quantity.trim())
                    appendLine()
                }
            }
        }
    }.trimEnd()

    // Locked notes are searched by title only, so a search can't reveal their content
    fun matches(note: Note, query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return false
        if (displayTitle(note).contains(q, ignoreCase = true)) return true
        if (note.isLocked) return false
        return note.body.contains(q, ignoreCase = true) ||
            note.items.any { it.text.contains(q, ignoreCase = true) }
    }

    private fun String.capitalizeFirst(): String =
        if (isEmpty()) this else substring(0, 1).uppercase() + substring(1)
}
