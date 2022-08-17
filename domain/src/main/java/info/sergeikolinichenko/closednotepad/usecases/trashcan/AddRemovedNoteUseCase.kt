package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository

class AddRemovedNoteUseCase(
    private val removedNoteRepository: RemovedNoteRepository
) {
    suspend operator fun invoke(removedNote: RemovedNote) {
        removedNoteRepository.addRemovedNote(removedNote)
    }
}