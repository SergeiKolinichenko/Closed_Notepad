package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository

class SearchNoteUseCase(private val noteRepository: NotesRepository) {

    suspend operator fun invoke(str: String): List<Note> {
        return noteRepository.searchNote(str)
    }
}