package info.sergeikolinichenko.closednotepad.repository

import android.graphics.Color
import android.util.Log
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry
import info.sergeikolinichenko.data.R
import java.util.*

object NoteRepositoryImpl : NoteRepository {

    private val noteList = mutableListOf<NoteEntry>()

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
                    timeStamp,
                    titleEntry,
                    itselfEntry,
                    colorIndex,
                    isLocked
                ))
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

    override fun getListNote(): List<NoteEntry> {
        return noteList.toList()
    }

    override fun getNoteEntry(timeStamp: Long): NoteEntry {
        return noteList.find {
            it.timeStamp == timeStamp
        } ?: throw RuntimeException("Entry with timeStamp $timeStamp not find")
    }

    override fun addEntryToNote(noteEntry: NoteEntry) {
        noteList.add(noteEntry)
    }

    override fun editEntryAtNote(noteEntry: NoteEntry) {
        val oldEntry = getNoteEntry(noteEntry.timeStamp)
        noteList.remove(oldEntry)
        addEntryToNote(noteEntry)
    }

    override fun removeEntryFromNote(noteEntry: NoteEntry): TrashEntry {
        noteList.remove(noteEntry)
        return TrashEntry(  // TODO() "need to rewrite it"
            0, 0, "title", "itself", 0
        )
    }

    override fun searchEntryAtNote(str: String): List<NoteEntry> {
        TODO("Not yet implemented")
    }
}