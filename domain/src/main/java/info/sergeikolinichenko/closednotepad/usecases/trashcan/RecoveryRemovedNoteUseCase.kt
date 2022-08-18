package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository

class RecoveryRemovedNoteUseCase(
    private val removedNoteRepository: RemovedNoteRepository
) {
    suspend operator fun invoke(removedNote: RemovedNote) =
        removedNoteRepository.recoveryRemovedNote(removedNote)
}