package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTextTest {

    private fun text(body: String, title: String = "", locked: Boolean = false) = Note(
        type = NoteType.TEXT, title = title, body = body, colorIndex = 0,
        isLocked = locked, createdAt = 0, updatedAt = 0
    )

    @Test
    fun derivedTitleHandlesInputsThatCrashedVersion2() {
        // A long first word followed by a space used to throw StringIndexOutOfBoundsException
        val url = "https://example.com/very/long/path/to/page some text"
        assertTrue(NoteText.deriveTitle(url).length <= NoteText.MAX_DERIVED_TITLE_LENGTH + 1)
        // Text starting with a dot used to produce an empty title and crash
        assertEquals(".hello world", NoteText.deriveTitle(".hello world"))
        assertEquals("", NoteText.deriveTitle(""))
        assertEquals("", NoteText.deriveTitle("   \n  "))
    }

    @Test
    fun derivedTitleUsesFirstLineAndCutsOnWord() {
        assertEquals("First line", NoteText.deriveTitle("\n first line\nsecond"))
        assertEquals(
            "Обсудить релиз, подготовить демо и…",
            NoteText.deriveTitle("обсудить релиз, подготовить демо и список багов")
        )
    }

    @Test
    fun displayTitleFallsBackToFirstItem() {
        val note = Note(
            type = NoteType.CHECKLIST, colorIndex = 0, createdAt = 0, updatedAt = 0,
            items = listOf(NoteItem("1", " "), NoteItem("2", "call mom"))
        )
        assertEquals("Call mom", NoteText.displayTitle(note))
    }

    @Test
    fun lockedNotesAreSearchedByTitleOnly() {
        assertTrue(NoteText.matches(text("my password 1234", title = "Wi-Fi"), "password"))
        assertFalse(NoteText.matches(text("my password 1234", title = "Wi-Fi", locked = true), "password"))
        assertTrue(NoteText.matches(text("my password 1234", title = "Wi-Fi", locked = true), "wi-fi"))
        assertFalse(NoteText.matches(text("anything"), "  "))
    }

    @Test
    fun plainTextOfList() {
        val note = Note(
            type = NoteType.SHOPPING, title = "Products", colorIndex = 0, createdAt = 0, updatedAt = 0,
            items = listOf(NoteItem("1", "Milk", isChecked = true), NoteItem("2", "Eggs", quantity = "10 pcs"))
        )
        assertEquals("Products\n\n☑ Milk\n☐ Eggs — 10 pcs", NoteText.toPlainText(note))
    }
}
