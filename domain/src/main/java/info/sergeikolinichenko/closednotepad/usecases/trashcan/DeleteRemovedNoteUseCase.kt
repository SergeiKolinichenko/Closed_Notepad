package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository
import javax.inject.Inject

class DeleteRemovedNoteUseCase @Inject constructor(
    private val removedNoteRepository: RemovedNoteRepository
) {
    suspend operator fun invoke(timeStamp: Long) {
        removedNoteRepository.deleteRemovedNote(timeStamp)
    }
}