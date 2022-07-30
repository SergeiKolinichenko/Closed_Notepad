package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.repository.NotesRepository

class AddNoteUseCase(private val noteRepository: NotesRepository) {

    suspend operator fun invoke(noteEntry: Note) {
        noteRepository.addNote(noteEntry)
    }
}