package info.sergeikolinichenko.closednotepad.repository

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry
import java.util.*

object NoteRepositoryImpl : NoteRepository {

    private val noteList = mutableListOf<NoteEntry>()

    init {
        for (element in 1..20) {
            val timeStamp = Date().time
            val titleEntry = "My Title $element"
            val itselfEntry = "This is itself entry $element and itself entry $element too"
            val colorIndex = Random().nextInt(256)
            noteList.add(
                NoteEntry(
                    timeStamp,
                    timeStamp,
                    timeStamp,
                    titleEntry,
                    itselfEntry,
                    colorIndex
                ))
        }
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