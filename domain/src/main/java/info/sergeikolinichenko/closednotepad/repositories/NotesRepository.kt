package info.sergeikolinichenko.closednotepad.repositories

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote

interface NotesRepository {

    // Get a collection of notebook notes
    fun <T> getListNotes(): T

    // Getting a notepad note
    suspend fun getNote(timeStamp: Long): Note

    // Adding a notepad note
    suspend fun addNote(note: Note)

    // Editing a notepad note
    suspend fun editNote(note: Note)

    // Move selected notes from notepad to trash
    suspend fun removeNote(note: Note): RemovedNote
}