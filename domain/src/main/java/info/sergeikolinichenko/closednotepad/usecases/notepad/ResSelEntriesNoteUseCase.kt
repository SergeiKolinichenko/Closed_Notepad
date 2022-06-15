package info.sergeikolinichenko.closednotepad.usecases.notepad

import info.sergeikolinichenko.closednotepad.repository.NoteRepository

class ResSelEntriesNoteUseCase(private val noteRepository: NoteRepository) {
    operator fun invoke() {
        noteRepository.resSelEntriesAtNote()
    }
}