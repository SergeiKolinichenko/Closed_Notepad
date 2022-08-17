package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository

class DeleteRemovedNoteUseCase(
    private val removedNoteRepository: RemovedNoteRepository
) {
    suspend operator fun invoke(timeStamp: Long) {
        removedNoteRepository.deleteRemovedNote(timeStamp)
    }
}