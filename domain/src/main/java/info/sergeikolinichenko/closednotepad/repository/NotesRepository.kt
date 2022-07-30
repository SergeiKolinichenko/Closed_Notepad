package info.sergeikolinichenko.closednotepad.repository

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote

interface NotesRepository {

    // Get a collection of notebook entries
    suspend fun getListNotes(): List<Note>

    // Getting a notepad entry
    suspend fun getNote(timeStamp: Long): Note

    // Adding a notepad entry
    suspend fun addNote(noteEntry: Note)

    // Editing a notepad entry
    suspend fun editNote(noteEntry: Note)

    // Move selected notes from notepad to trash
    suspend fun removeNote(noteEntry: Note)

    // Searching for a required note in a notebook
    suspend fun searchNote(str: String): List<Note>
}