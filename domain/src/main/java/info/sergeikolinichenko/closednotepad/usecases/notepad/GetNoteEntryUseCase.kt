package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.repository.NoteRepository

class GetNoteEntryUseCase(private val noteRepository: NoteRepository) {

    operator fun invoke(timeStamp: Int): NoteEntry {
        return noteRepository.getNoteEntry(timeStamp)
    }
}