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

    // Select an entry in the notebook collection for further actions
    fun selectEntryAtNote(noteEntry: NoteEntry)

    // Cancel select entry in notepad collection
    fun resSelEntriesAtNote()

    // Move selected notes from notepad to trash
    fun removeEntryFromNote(noteEntry: NoteEntry): TrashEntry

    // Move note from notepad to trash
    fun removeEntriesFromNote(): List<TrashEntry>

    // Searching for a required note in a notebook
    fun searchEntryAtNote(str: String): List<NoteEntry>
}