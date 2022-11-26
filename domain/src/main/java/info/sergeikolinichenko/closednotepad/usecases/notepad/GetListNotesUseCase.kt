package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import javax.inject.Inject

class GetListNotesUseCase @Inject constructor(
    private val noteRepository: NotesRepository
    ) {

    operator fun <T> invoke (): T {
        return noteRepository.getListNotes()
    }
}