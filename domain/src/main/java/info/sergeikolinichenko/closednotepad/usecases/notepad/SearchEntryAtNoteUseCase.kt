package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.repository.NoteRepository

class SearchEntryAtNoteUseCase(private val noteRepository: NoteRepository) {

    operator fun invoke(str: String): List<NoteEntry> {
        return noteRepository.searchEntryAtNote(str)
    }
}