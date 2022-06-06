package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.models.TrashEntry
import info.sergeikolinichenko.closednotepad.repository.NoteRepository

class RemoveEntryFromNoteUseCase(private val noteRepository: NoteRepository) {

    operator fun invoke(noteEntry: NoteEntry): TrashEntry {
        return noteRepository.removeEntryFromNote(noteEntry)
    }
}