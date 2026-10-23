package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Notes as Markdown files that any other app can read. A short header keeps what Markdown
// has no place for (type, color, dates), so an exported note comes back the same on import.
object NoteMarkdown {

    const val EXTENSION = "md"
    private const val FENCE = "---"
    private const val MAX_FILE_NAME_LENGTH = 60
    private const val QUANTITY_SEPARATOR = " — "
    private const val PRICE_SEPARATOR = " · "

    private val taskLine = Regex("""^\s*[-*+]\s+\[([ xX])]\s?(.*)$""")
    private val priceSuffix = Regex("""^(.*)$PRICE_SEPARATOR(\d+(?:\.\d+)?)$""")
    private val plainBullets = listOf(NoteConverter.CHECKED_MARK, NoteConverter.UNCHECKED_MARK, "• ", "- ", "* ")
    private val unsafeFileChars = Regex("""[\\/:*?"<>|\u0000-\u001F]""")

    fun fileName(note: Note): String {
        val title = NoteText.displayTitle(note)
            .removeSuffix("…")
            .replace(unsafeFileChars, " ")
            .replace(Regex("""\s+"""), " ")
            .trim(' ', '.')
            .take(MAX_FILE_NAME_LENGTH)
            .trim()
        return (title.ifEmpty { "note-${note.id}" }) + ".$EXTENSION"
    }

    fun toMarkdown(note: Note): String = buildString {
        appendLine(FENCE)
        appendLine("type: ${note.type.name.lowercase()}")
        if (note.title.isNotBlank()) appendLine("title: ${quote(note.title.trim())}")
        appendLine("color: ${note.colorIndex}")
        if (note.isPinned) appendLine("pinned: true")
        if (note.isLocked) appendLine("locked: true")
        appendLine("created: ${formatTime(note.createdAt)}")
        appendLine("updated: ${formatTime(note.updatedAt)}")
        appendLine(FENCE)
        if (note.title.isNotBlank()) {
            appendLine("# ${note.title.trim()}")
            appendLine()
        }
        when (note.type) {
            NoteType.TEXT -> appendLine(note.body.trimEnd())
            NoteType.CHECKLIST -> note.items.filter { it.text.isNotBlank() }.forEach { appendLine(itemLine(it)) }
            NoteType.SHOPPING -> {
                var section = ""
                note.items.filter { it.text.isNotBlank() }
                    .sortedBy { it.section.isNotBlank() } // keeps the order inside each section
                    .groupBy { it.section.trim() }
                    .forEach { (name, items) ->
                        if (name != section) {
                            appendLine()
                            appendLine("## $name")
                            section = name
                        }
                        items.forEach { appendLine(itemLine(it)) }
                    }
            }
        }
    }

    // Reads a file written by toMarkdown, or any plain text or Markdown file.
    // A file of task lines ("- [ ] milk") becomes a to-do list.
    fun fromMarkdown(
        text: String,
        defaultColorIndex: Int,
        now: Long,
        newItemId: () -> String
    ): Note {
        val lines = text.replace("\r\n", "\n").replace('\r', '\n').removePrefix("﻿").lines()
        val header = readHeader(lines)
        var rest = if (header != null) lines.drop(header.size + 2) else lines
        rest = rest.dropWhile { it.isBlank() }

        val headerTitle = header?.get("title")?.let { unquote(it) }
        var title = headerTitle.orEmpty()
        val heading = rest.firstOrNull()?.takeIf { it.startsWith("# ") }?.removePrefix("# ")?.trim()
        // Without our header the first heading is the title; with it, only a heading repeating the title
        if (heading != null && (header == null || heading == headerTitle)) {
            title = heading
            rest = rest.drop(1).dropWhile { it.isBlank() }
        }
        val content = rest.joinToString("\n").trimEnd()

        val type = header?.get("type")?.let { value -> NoteType.entries.firstOrNull { it.name.equals(value, true) } }
            ?: if (looksLikeList(rest)) NoteType.CHECKLIST else NoteType.TEXT
        val createdAt = header?.get("created")?.let { parseTime(it) } ?: now
        return Note(
            type = type,
            title = title,
            body = if (type == NoteType.TEXT) content else "",
            items = if (type == NoteType.TEXT) emptyList() else readItems(rest, type, newItemId),
            colorIndex = header?.get("color")?.toIntOrNull()?.takeIf { it in 0..7 } ?: defaultColorIndex,
            isPinned = header?.get("pinned") == "true",
            isLocked = header?.get("locked") == "true",
            createdAt = createdAt,
            updatedAt = header?.get("updated")?.let { parseTime(it) } ?: createdAt
        )
    }

    // Same note again: used to skip files that were already imported
    fun sameContent(a: Note, b: Note): Boolean =
        a.type == b.type && a.title.trim() == b.title.trim() && a.body.trim() == b.body.trim() &&
            a.items.map { it.text.trim() to it.quantity.trim() } == b.items.map { it.text.trim() to it.quantity.trim() }

    private fun itemLine(item: NoteItem): String = buildString {
        append(if (item.isChecked) "- [x] " else "- [ ] ")
        append(item.text.trim().replace('\n', ' '))
        if (item.quantity.isNotBlank()) append(QUANTITY_SEPARATOR).append(item.quantity.trim())
        item.price?.let { append(PRICE_SEPARATOR).append(formatPrice(it)) }
    }

    private fun readItems(lines: List<String>, type: NoteType, newItemId: () -> String): List<NoteItem> {
        var section = ""
        val items = mutableListOf<NoteItem>()
        lines.forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("## ")) {
                section = line.removePrefix("## ").trim()
                return@forEach
            }
            if (line.isEmpty()) return@forEach
            val task = taskLine.matchEntire(line)
            val checked = if (task != null) task.groupValues[1] != " " else line.startsWith(NoteConverter.CHECKED_MARK)
            var text = task?.groupValues?.get(2)?.trim()
                ?: plainBullets.firstOrNull { line.startsWith(it) }?.let { line.removePrefix(it).trim() }
                ?: line
            var quantity = ""
            var price: Double? = null
            if (type == NoteType.SHOPPING) {
                priceSuffix.matchEntire(text)?.let { m ->
                    text = m.groupValues[1]
                    price = m.groupValues[2].toDoubleOrNull()
                }
                val at = text.lastIndexOf(QUANTITY_SEPARATOR)
                if (at > 0) {
                    quantity = text.substring(at + QUANTITY_SEPARATOR.length).trim()
                    text = text.substring(0, at).trim()
                }
            }
            items += NoteItem(
                id = newItemId(),
                text = text,
                isChecked = checked,
                quantity = quantity,
                section = if (type == NoteType.SHOPPING) section else "",
                price = price
            )
        }
        return items
    }

    private fun looksLikeList(lines: List<String>): Boolean {
        val filled = lines.filter { it.isNotBlank() }
        return filled.isNotEmpty() && filled.all { taskLine.matches(it) }
    }

    // The lines between the two "---" at the very top, as key -> value
    private fun readHeader(lines: List<String>): Map<String, String>? {
        if (lines.firstOrNull()?.trim() != FENCE) return null
        val end = lines.drop(1).indexOfFirst { it.trim() == FENCE }
        if (end < 0) return null
        val values = lines.subList(1, end + 1).mapNotNull { line ->
            val at = line.indexOf(':')
            if (at <= 0) null else line.substring(0, at).trim() to line.substring(at + 1).trim()
        }.toMap()
        // A header written by another app (e.g. Obsidian) is skipped too; unknown keys are ignored
        return values
    }

    private fun quote(value: String): String =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""

    private fun unquote(value: String): String {
        if (value.length < 2 || !value.startsWith("\"") || !value.endsWith("\"")) return value
        val inner = value.substring(1, value.length - 1)
        return buildString {
            var i = 0
            while (i < inner.length) {
                val c = inner[i]
                if (c == '\\' && i + 1 < inner.length) {
                    val next = inner[i + 1]
                    append(if (next == 'n') '\n' else next)
                    i += 2
                } else {
                    append(c)
                    i++
                }
            }
        }
    }

    private fun formatPrice(value: Double): String =
        if (value == Math.floor(value)) value.toLong().toString() else value.toBigDecimal().stripTrailingZeros().toPlainString()

    // java.time needs Android 8, the app runs on Android 6
    private fun timeFormat() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun formatTime(time: Long): String = timeFormat().format(time)

    private fun parseTime(value: String): Long? = try {
        timeFormat().parse(value)?.time
    } catch (e: ParseException) {
        null
    }
}
