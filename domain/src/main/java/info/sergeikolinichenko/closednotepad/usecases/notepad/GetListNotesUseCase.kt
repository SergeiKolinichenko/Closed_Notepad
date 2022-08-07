package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.repositories.NotesRepository

class GetListNotesUseCase(private val noteRepository: NotesRepository) {

    operator fun <T> invoke (): T {
        return noteRepository.getListNotes()
    }
}