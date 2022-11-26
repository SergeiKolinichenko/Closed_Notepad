package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import javax.inject.Inject

class EditNoteUseCase @Inject constructor(private val noteRepository: NotesRepository) {

    suspend operator fun invoke(noteEntry: Note) {
        noteRepository.editNote(noteEntry)
    }
}