package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository
import javax.inject.Inject

class GetRemovedNoteUseCase @Inject constructor(
    private val removedNoteRepository: RemovedNoteRepository
) {

    suspend operator fun invoke(timeStamp: Long): RemovedNote {
        return removedNoteRepository.getRemovedNote(timeStamp)
    }
}