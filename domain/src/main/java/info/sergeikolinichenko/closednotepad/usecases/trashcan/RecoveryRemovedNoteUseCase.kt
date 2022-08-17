package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository

class RecoveryRemovedNoteUseCase(
    private val removedNoteRepository: RemovedNoteRepository
) {
    suspend operator fun invoke(timeStamp: Long) =
        removedNoteRepository.recoveryRemovedNote(timeStamp)
}