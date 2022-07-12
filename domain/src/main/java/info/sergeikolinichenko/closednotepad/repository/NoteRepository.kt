package info.sergeikolinichenko.closednotepad.repository

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry

interface NoteRepository {

    // Get a collection of notebook entries
    fun getListNote(): List<NoteEntry>

    // Getting a notepad entry
    fun getNoteEntry(timeStamp: Long): NoteEntry

    // Adding a notepad entry
    fun addEntryToNote(noteEntry: NoteEntry)

    // Editing a notepad entry
    fun editEntryAtNote(noteEntry: NoteEntry)

    // Move selected notes from notepad to trash
    fun removeEntryFromNote(noteEntry: NoteEntry): TrashEntry

    // Searching for a required note in a notebook
    fun searchEntryAtNote(str: String): List<NoteEntry>
}