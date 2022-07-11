package info.sergeikolinichenko.closednotepad.repository

import android.app.Application
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry

class NoteRepositoryImpl(application: Application) : NoteRepository {

    private val noteEntriesDao = AppDatabase.getInstance(application).noteEntriesDao()
    private val mapper = NoteEntryMapper()

    private val noteList =
        sortedSetOf<NoteEntry>({ o1, o2 -> o1.timeStamp.compareTo(o2.timeStamp) })

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
        noteEntriesDao.deleteNoteEntry(noteEntry.timeStamp)
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