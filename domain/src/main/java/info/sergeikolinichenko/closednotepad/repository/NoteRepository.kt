package info.sergeikolinichenko.closednotepad.repository

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry

interface NoteRepository {

    fun getListNote(): List<NoteEntry>

    fun getNoteEntry(timeStamp: Long): NoteEntry

    fun addEntryToNote(noteEntry: NoteEntry)

    fun editEntryAtNote(noteEntry: NoteEntry)

    fun removeEntryFromNote(noteEntry: NoteEntry): TrashEntry

    fun searchEntryAtNote(str: String): List<NoteEntry>
}