package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository

class EditNoteUseCase(private val noteRepository: NotesRepository) {

    suspend operator fun invoke(noteEntry: Note) {
        noteRepository.editNote(noteEntry)
    }
}