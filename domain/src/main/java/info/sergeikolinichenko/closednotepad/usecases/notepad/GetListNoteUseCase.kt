package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.repository.NoteRepository

class GetListNoteUseCase(private val noteRepository: NoteRepository) {

    operator fun invoke (): List<NoteEntry> {
        return noteRepository.getListNote()
    }
}