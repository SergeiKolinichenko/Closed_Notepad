package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteMarkdownTest {

    private var counter = 0
    private fun newId() = "id${counter++}"

    private fun roundTrip(note: Note): Note =
        NoteMarkdown.fromMarkdown(NoteMarkdown.toMarkdown(note), defaultColorIndex = 0, now = 0, newItemId = ::newId)

    @Test
    fun textNoteComesBackTheSame() {
        val note = Note(
            type = NoteType.TEXT,
            title = "Plans: \"May\"",
            body = "# Not a title\n\nLine two",
            colorIndex = 3,
            isPinned = true,
            isLocked = true,
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_000_100_000
        )
        assertEquals(note, roundTrip(note))
    }

    @Test
    fun untitledTextKeepsItsHeading() {
        val note = Note(type = NoteType.TEXT, body = "# Heading\ntext", colorIndex = 1, createdAt = 0, updatedAt = 0)
        val back = roundTrip(note)
        assertEquals("", back.title)
        assertEquals("# Heading\ntext", back.body)
    }

    @Test
    fun shoppingListKeepsQuantitiesPricesAndSections() {
        val note = Note(
            type = NoteType.SHOPPING,
            title = "Market",
            items = listOf(
                NoteItem("a", "Bread"),
                NoteItem("b", "Milk", isChecked = true, quantity = "2 l", section = "Dairy", price = 1.5),
                NoteItem("c", "Cheese", section = "Dairy", price = 7.0)
            ),
            colorIndex = 2,
            createdAt = 0,
            updatedAt = 0
        )
        val markdown = NoteMarkdown.toMarkdown(note)
        assertTrue(markdown, markdown.contains("## Dairy\n- [x] Milk — 2 l · 1.5\n- [ ] Cheese · 7\n"))
        val back = roundTrip(note)
        assertEquals(note.items.map { it.copy(id = "") }, back.items.map { it.copy(id = "") })
    }

    @Test
    fun plainFilesFromOtherApps() {
        val text = NoteMarkdown.fromMarkdown("# Ideas\r\n\r\nfirst\r\nsecond", 6, now = 5, newItemId = ::newId)
        assertEquals(NoteType.TEXT, text.type)
        assertEquals("Ideas", text.title)
        assertEquals("first\nsecond", text.body)
        assertEquals(6, text.colorIndex)
        assertEquals(5, text.createdAt)

        val list = NoteMarkdown.fromMarkdown("- [ ] milk\n- [X] bread\n", 6, now = 5, newItemId = ::newId)
        assertEquals(NoteType.CHECKLIST, list.type)
        assertEquals(listOf("milk", "bread"), list.items.map { it.text })
        assertEquals(listOf(false, true), list.items.map { it.isChecked })

        val obsidian = NoteMarkdown.fromMarkdown("---\ntags: [a]\n---\nBody", 6, now = 5, newItemId = ::newId)
        assertEquals("Body", obsidian.body)
    }

    @Test
    fun fileNamesAreSafe() {
        val note = Note(type = NoteType.TEXT, title = "a/b: c?", colorIndex = 0, createdAt = 0, updatedAt = 0)
        assertEquals("a b c.md", NoteMarkdown.fileName(note))
        assertEquals("note-7.md", NoteMarkdown.fileName(note.copy(id = 7, title = "")))
    }
}
