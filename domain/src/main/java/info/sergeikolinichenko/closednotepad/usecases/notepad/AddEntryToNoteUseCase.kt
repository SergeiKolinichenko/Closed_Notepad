package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.repository.NoteRepository

class AddEntryToNoteUseCase(private val noteRepository: NoteRepository) {

    operator fun invoke(noteEntry: NoteEntry) {
        noteRepository.addEntryToNote(noteEntry)
    }
}