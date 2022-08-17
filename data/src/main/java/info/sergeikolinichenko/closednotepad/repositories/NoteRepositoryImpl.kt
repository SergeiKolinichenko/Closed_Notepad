package info.sergeikolinichenko.closednotepad.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.utils.NoteMapper

class NoteRepositoryImpl(application: Application) : NotesRepository {

    private val notesDao = AppDatabase.getInstance(application).noteEntriesDao()
    private val mapper = NoteMapper()

    // Implementation of getting a collection of notebook entries

    @Suppress("UNCHECKED_CAST")
    override fun <T> getListNotes(): T {
        val noteList: LiveData<List<Note>> = Transformations.map(notesDao.getNoteList()) {
            mapper.mapListDbModelToListEntity(it)
        }
        return noteList as T
    }

    // Implementation of getting a notepad entry
    override suspend fun getNote(timeStamp: Long): Note {
        return mapper.mapDbModelToEntity(notesDao.getNote(timeStamp))
    }

    // Implementation of adding a notepad entry
    override suspend fun addNote(note: Note) {
        notesDao.addNote(mapper.mapEntityToDbModel(note))
    }

    // Implementation of editing a notepad entry
    override suspend fun editNote(note: Note) {
        notesDao.addNote(mapper.mapEntityToDbModel(note))
    }

    // Implementation of removing note from notepad to trash
    override suspend fun removeNote(note: Note): RemovedNote {
        val removedNote = RemovedNote(
            timeStamp = note.timeStamp,
            titleEntry = note.titleNote,
            itselfEntry = note.itselfNote,
            colorIndex = note.colorIndex,
            isLocked = note.isLocked
        )
        notesDao.removedNote(note.timeStamp)
        return removedNote
    }
}