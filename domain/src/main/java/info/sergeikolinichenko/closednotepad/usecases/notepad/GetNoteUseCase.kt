package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository

class GetNoteUseCase(private val noteRepository: NotesRepository) {

    suspend operator fun invoke(timeStamp: Long): Note {
        return noteRepository.getNote(timeStamp)
    }
}