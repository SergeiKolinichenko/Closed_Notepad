package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.repository.NotesRepository

class RemoveNoteUseCase(private val noteRepository: NotesRepository) {

    suspend operator fun invoke(noteEntry: Note) {
        return noteRepository.removeNote(noteEntry)
    }
}