package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.repository.NotesRepository

class GetListNotesUseCase(private val noteRepository: NotesRepository) {

    operator fun <T> invoke (): T {
        return noteRepository.getListNotes()
    }
}