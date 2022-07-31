package info.sergeikolinichenko.closednotepad.repository

import android.app.Application
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.models.Note

class NoteRepositoryImpl(application: Application) : NotesRepository {

    private val notesDao = AppDatabase.getInstance(application).noteEntriesDao()
    private val mapper = NoteMapper()

    // Implementation of getting a collection of notebook entries
    override suspend fun getListNotes(): List<Note> {
        return mapper.mapListDbModelToListEntity(notesDao.getNoteList())
    }

    // Implementation of getting a notepad entry
    override suspend fun getNote(timeStamp: Long): Note {
        return mapper.mapDbModelToEntity(notesDao.getNoteEntry(timeStamp))
    }

    // Implementation of adding a notepad entry
    override suspend fun addNote(noteEntry: Note) {
        notesDao.addNoteEntry(mapper.mapEntityToDbModel(noteEntry))
    }

    // Implementation of editing a notepad entry
    override suspend fun editNote(noteEntry: Note) {
        notesDao.addNoteEntry(mapper.mapEntityToDbModel(noteEntry))
    }

    // Implementation of removing note from notepad to trash
    override suspend fun removeNote(noteEntry: Note) {
        notesDao.deleteNoteEntry(noteEntry.timeStamp)
    }

    // Implementation of searching for a required note in a notebook
    override suspend fun searchNote(str: String): List<Note> {
        TODO("Not yet implemented")
    }
}