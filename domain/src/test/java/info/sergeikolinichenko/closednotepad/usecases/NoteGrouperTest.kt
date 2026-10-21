package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.models.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NoteGrouperTest {

    private val zone = TimeZone.getTimeZone("Europe/Moscow")
    private val calendar = { Calendar.getInstance(zone, Locale.forLanguageTag("ru-RU")) } // weeks start on Monday

    private fun at(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        calendar().apply { clear(); set(year, month - 1, day, hour, 0) }.timeInMillis

    private fun note(id: Long, updated: Long, pinned: Boolean = false) = Note(
        id = id, type = NoteType.TEXT, colorIndex = 0, isPinned = pinned, createdAt = updated, updatedAt = updated
    )

    @Test
    fun groupsByDay() {
        val now = at(2026, 10, 21, 15) // Wednesday
        val notes = listOf(
            note(1, at(2026, 10, 1), pinned = true),
            note(2, at(2026, 10, 21, 9)),
            note(3, at(2026, 10, 21, 0)),
            note(4, at(2026, 10, 20, 23)),
            note(5, at(2026, 10, 19, 1)),
            note(6, at(2026, 10, 18)),
            note(7, at(2026, 10, 2)),
            note(8, at(2026, 9, 30)),
            note(9, at(2025, 9, 3))
        )
        val groups = NoteGrouper.group(NoteSorter.sort(notes, SortOrder.UPDATED_DESC), SortOrder.UPDATED_DESC, now, calendar)
        assertEquals(
            listOf(
                NoteGroup.Pinned to listOf(1L),
                NoteGroup.Today to listOf(2L, 3L),
                NoteGroup.Yesterday to listOf(4L),
                NoteGroup.ThisWeek to listOf(5L),
                NoteGroup.ThisMonth to listOf(6L, 7L),
                NoteGroup.Month(2026, Calendar.SEPTEMBER) to listOf(8L),
                NoteGroup.Month(2025, Calendar.SEPTEMBER) to listOf(9L)
            ),
            groups.map { (group, list) -> group to list.map { it.id } }
        )
    }

    @Test
    fun oldestFirstKeepsTheOrder() {
        val now = at(2026, 10, 21)
        val notes = listOf(note(1, at(2026, 8, 5)), note(2, at(2026, 10, 21)))
        val groups = NoteGrouper.group(NoteSorter.sort(notes, SortOrder.CREATED_ASC), SortOrder.CREATED_ASC, now, calendar)
        assertEquals(listOf(NoteGroup.Month(2026, Calendar.AUGUST), NoteGroup.Today), groups.map { it.first })
    }

    @Test
    fun noDatesWhenSortedByTitle() {
        val notes = listOf(note(1, 0), note(2, 0, pinned = true))
        assertEquals(
            listOf(NoteGroup.Pinned, NoteGroup.Others),
            NoteGrouper.group(notes, SortOrder.TITLE_ASC, 0, calendar).map { it.first }
        )
        assertEquals(listOf(null), NoteGrouper.group(listOf(note(1, 0)), SortOrder.COLOR, 0, calendar).map { it.first })
    }
}
