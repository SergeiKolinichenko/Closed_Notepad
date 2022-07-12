package info.sergeikolinichenko.closednotepad.repository

import android.app.Application
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry

class NoteRepositoryImpl(application: Application) : NoteRepository {

    private val noteEntriesDao = AppDatabase.getInstance(application).noteEntriesDao()
    private val mapper = NoteEntryMapper()

//    private val noteList =
//        sortedSetOf<NoteEntry>({ o1, o2 -> o1.timeStamp.compareTo(o2.timeStamp) })

    // Implementation of getting a collection of notebook entries
    override fun getListNote(): List<NoteEntry> {
        return mapper.mapListDbModelToListEntity(noteEntriesDao.getNoteList())
    }

    // Implementation of getting a notepad entry
    override fun getNoteEntry(timeStamp: Long): NoteEntry {
        return mapper.mapDbModelToEntity(noteEntriesDao.getNoteEntry(timeStamp))
    }

    // Implementation of adding a notepad entry
    override fun addEntryToNote(noteEntry: NoteEntry) {
        noteEntriesDao.addNoteEntry(mapper.mapEntityToDbModel(noteEntry))
    }

    // Implementation of editing a notepad entry
    override fun editEntryAtNote(noteEntry: NoteEntry) {
        noteEntriesDao.addNoteEntry(mapper.mapEntityToDbModel(noteEntry))
    }

    // Implementation of removing note from notepad to trash
    override fun removeEntryFromNote(noteEntry: NoteEntry): TrashEntry {
        noteEntriesDao.deleteNoteEntry(noteEntry.timeStamp)
        return TrashEntry(  // TODO() "need to rewrite it"
            0, "title", "itself", 0
        )
    }

    // Implementation of searching for a required note in a notebook
    override fun searchEntryAtNote(str: String): List<NoteEntry> {
        TODO("Not yet implemented")
    }
}