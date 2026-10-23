package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.models.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LogicTest {

    @Test
    fun shoppingInputParser() {
        assertEquals(ShoppingInputParser.Parsed("Яйца", "10 шт"), ShoppingInputParser.parse("яйца 10 шт"))
        assertEquals(ShoppingInputParser.Parsed("Молока", "2 л"), ShoppingInputParser.parse("2л молока"))
        assertEquals(ShoppingInputParser.Parsed("Milk", "1.5 l"), ShoppingInputParser.parse(" milk  1.5 l "))
        assertEquals(ShoppingInputParser.Parsed("Хлеб", ""), ShoppingInputParser.parse("хлеб"))
        assertEquals(ShoppingInputParser.Parsed("Батарейки AA", "4"), ShoppingInputParser.parse("Батарейки AA 4"))
    }

    @Test
    fun textAndListConvertBothWays() {
        var counter = 0
        val text = Note(
            type = NoteType.TEXT, body = "- milk\n\n☑ bread\nEggs", colorIndex = 0, createdAt = 0, updatedAt = 0
        )
        val list = NoteConverter.textToList(text) { "id${counter++}" }
        assertEquals(listOf("milk", "bread", "Eggs"), list.items.map { it.text })
        assertEquals(listOf(false, true, false), list.items.map { it.isChecked })

        val back = NoteConverter.listToText(list)
        assertEquals("☐ milk\n☑ bread\n☐ Eggs", back.body)
        assertEquals(list.items.map { it.isChecked }, NoteConverter.textToList(back) { "x" }.items.map { it.isChecked })
    }

    @Test
    fun trashPolicy() {
        val day = TrashPolicy.DAY_MILLIS
        assertNull(TrashPolicy.deleteBefore(now = 100 * day, daysBeforeDeletion = 0))
        assertEquals(70 * day, TrashPolicy.deleteBefore(now = 100 * day, daysBeforeDeletion = 30))
        assertEquals(30, TrashPolicy.daysLeft(removedAt = 0, now = 0, daysBeforeDeletion = 30))
        assertEquals(1, TrashPolicy.daysLeft(removedAt = 0, now = 29 * day + 1, daysBeforeDeletion = 30))
        assertEquals(0, TrashPolicy.daysLeft(removedAt = 0, now = 31 * day, daysBeforeDeletion = 30))
    }

    @Test
    fun pinnedNotesGoFirst() {
        fun note(id: Long, pinned: Boolean, updated: Long) = Note(
            id = id, type = NoteType.TEXT, colorIndex = 0, isPinned = pinned, createdAt = id, updatedAt = updated
        )
        val sorted = NoteSorter.sort(
            listOf(note(1, false, 30), note(2, true, 10), note(3, false, 20)),
            SortOrder.UPDATED_DESC
        )
        assertEquals(listOf(2L, 1L, 3L), sorted.map { it.id })
    }
}
