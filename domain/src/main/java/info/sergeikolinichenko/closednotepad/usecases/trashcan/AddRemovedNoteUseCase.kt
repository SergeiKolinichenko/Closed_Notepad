package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository
import javax.inject.Inject

class AddRemovedNoteUseCase @Inject constructor(
    private val removedNoteRepository: RemovedNoteRepository
) {
    suspend operator fun invoke(removedNote: RemovedNote) {
        removedNoteRepository.addRemovedNote(removedNote)
    }
}