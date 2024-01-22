package info.sergeikolinichenko.closednotepad.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import info.sergeikolinichenko.closednotepad.database.NotesDao
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.utils.NoteMapper
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val mapper: NoteMapper,
    private val notesDao: NotesDao
) : NotesRepository {

    // Implementation of getting a collection of notebook entries

    @Suppress("UNCHECKED_CAST")
    override fun <T> getListNotes(): T {
        val noteList: LiveData<List<Note>> =
            notesDao.getNoteList().map {
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
        notesDao.deleteNote(note.timeStamp)
        return mapper.mapNoteToRemovedNote(note)
    }
}