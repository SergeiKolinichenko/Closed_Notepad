package info.sergeikolinichenko.closednotepad.repository

import android.graphics.Color
import android.util.Log
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry
import info.sergeikolinichenko.data.R
import java.util.*

object NoteRepositoryImpl : NoteRepository {

    private val noteList =
        sortedSetOf<NoteEntry>({ o1, o2 -> o1.timeStamp.compareTo(o2.timeStamp) })

    init {
        for (element in 1..200) {
            val timeStamp = Date().time
            val titleEntry = "My Title $element"
            val itselfEntry = "This is itself entry $element and itself entry $element too"
            val colorIndex = getRandomColor()
            val isLocked = Random().nextBoolean()
            noteList.add(
                NoteEntry(
                    timeStamp,
                    titleEntry,
                    itselfEntry,
                    colorIndex,
                    isLocked,
                    false
                )
            )
            Thread.sleep(1)
        }
    }

    private fun getRandomColor(): Int {
        val listColor = mutableListOf<Int>()
        listColor.add(Color.RED)
        listColor.add(Color.BLUE)
        listColor.add(Color.CYAN)
        listColor.add(Color.GREEN)
        listColor.add(Color.MAGENTA)
        return listColor[Random().nextInt(listColor.size)]
    }

    // Implementation of getting a collection of notebook entries
    override fun getListNote(): List<NoteEntry> {
        return noteList.toList()
    }

    // Implementation of getting a notepad entry
    override fun getNoteEntry(timeStamp: Long): NoteEntry {
        return noteList.find {
            it.timeStamp == timeStamp
        } ?: throw RuntimeException("Entry with timeStamp $timeStamp not find")
    }

    // Implementation of adding a notepad entry
    override fun addEntryToNote(noteEntry: NoteEntry) {
        noteList.add(noteEntry)
    }

    // Implementation of editing a notepad entry
    override fun editEntryAtNote(noteEntry: NoteEntry) {
        val oldEntry = getNoteEntry(noteEntry.timeStamp)
        noteList.remove(oldEntry)
        addEntryToNote(noteEntry)
    }

    // Implementation of selecting an entry in the notebook collection for further actions
    override fun selectEntryAtNote(noteEntry: NoteEntry) {
        val newEntry = noteEntry.copy(isSelected = !noteEntry.isSelected)
        editEntryAtNote(newEntry)
    }

    // Implementation of canceling select entry in notepad collection
    override fun resSelEntriesAtNote() {
        val list = noteList.filter { it.isSelected }
        for (e in list) {
            selectEntryAtNote(e)
        }
        getListNote()
    }

    // Implementation of removing note from notepad to trash
    override fun removeEntryFromNote(noteEntry: NoteEntry): TrashEntry {
        noteList.remove(noteEntry)
        return TrashEntry(  // TODO() "need to rewrite it"
            0, "title", "itself", 0
        )
    }

    // Implementation of removing selected notes from notepad to trash
    override fun removeEntriesFromNote(): List<TrashEntry> {
        val list = noteList.filter { it.isSelected }
        for (e in list) {
            removeEntryFromNote(e)
        }
        // -------------------------------------------------
        return list.map {
            TrashEntry(
                timeStamp = it.timeStamp,
                titleEntry = it.titleEntry,
                itselfEntry = it.itselfEntry,
                colorIndex = it.colorIndex
            )
        }
    }

    // Implementation of searching for a required note in a notebook
    override fun searchEntryAtNote(str: String): List<NoteEntry> {
        TODO("Not yet implemented")
    }
}