package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import javax.inject.Inject

class GetNoteUseCase @Inject constructor(private val noteRepository: NotesRepository) {

    suspend operator fun invoke(timeStamp: Long): Note {
        return noteRepository.getNote(timeStamp)
    }
}