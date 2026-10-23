package info.sergeikolinichenko.closednotepad.usecases

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.SortOrder
import java.util.Calendar

sealed interface NoteGroup {
    data object Pinned : NoteGroup
    data object Others : NoteGroup
    data object Today : NoteGroup
    data object Yesterday : NoteGroup
    data object ThisWeek : NoteGroup
    data object ThisMonth : NoteGroup

    // month is 0-based, as in Calendar
    data class Month(val year: Int, val month: Int) : NoteGroup
}

object NoteGrouper {

    // Pinned notes first, then the rest by the date the list is sorted by: today, yesterday,
    // this week, this month, then month by month. Lists sorted by title or color get no date
    // groups. A null group means a plain list without a header.
    fun group(
        notes: List<Note>,
        order: SortOrder,
        now: Long = System.currentTimeMillis(),
        calendar: () -> Calendar = { Calendar.getInstance() }
    ): List<Pair<NoteGroup?, List<Note>>> {
        val pinned = notes.filter { it.isPinned }
        val others = notes.filter { !it.isPinned }
        val result = mutableListOf<Pair<NoteGroup?, List<Note>>>()
        if (pinned.isNotEmpty()) result += NoteGroup.Pinned to pinned
        if (others.isEmpty()) return result

        val dateOf: ((Note) -> Long)? = when (order) {
            SortOrder.UPDATED_DESC -> { note -> note.updatedAt }
            SortOrder.CREATED_DESC, SortOrder.CREATED_ASC -> { note -> note.createdAt }
            SortOrder.TITLE_ASC, SortOrder.TITLE_DESC, SortOrder.COLOR -> null
        }
        if (dateOf == null) {
            result += (if (pinned.isEmpty()) null else NoteGroup.Others) to others
            return result
        }

        val keyOf = groupKeys(now, calendar)
        // The notes are already sorted by this date, so equal groups follow each other
        var current: NoteGroup? = null
        var bucket = mutableListOf<Note>()
        others.forEach { note ->
            val key = keyOf(dateOf(note))
            if (key != current && bucket.isNotEmpty()) {
                result += current to bucket
                bucket = mutableListOf()
            }
            current = key
            bucket += note
        }
        result += current to bucket
        return result
    }

    private fun groupKeys(now: Long, calendar: () -> Calendar): (Long) -> NoteGroup {
        val today = calendar().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = today.timeInMillis
        val startOfYesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
        val startOfWeek = (today.clone() as Calendar).apply {
            val daysIntoWeek = (get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + 7) % 7
            add(Calendar.DAY_OF_YEAR, -daysIntoWeek)
        }.timeInMillis
        val startOfMonth = (today.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
        val date = calendar()
        return { time ->
            when {
                time >= startOfToday -> NoteGroup.Today
                time >= startOfYesterday -> NoteGroup.Yesterday
                time >= startOfWeek -> NoteGroup.ThisWeek
                time >= startOfMonth -> NoteGroup.ThisMonth
                else -> {
                    date.timeInMillis = time
                    NoteGroup.Month(date.get(Calendar.YEAR), date.get(Calendar.MONTH))
                }
            }
        }
    }
}
