package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 03.12.2022 at 22:28 (GMT+3) **/

class DeleteRemoveNotesUseCase @Inject constructor(
private val removedNoteRepository: RemovedNoteRepository
) {
    operator fun invoke() {
        removedNoteRepository.deleteRemovedNotes()
    }
}