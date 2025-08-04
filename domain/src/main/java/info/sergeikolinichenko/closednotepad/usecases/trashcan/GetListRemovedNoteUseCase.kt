package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository
import javax.inject.Inject

class GetListRemovedNoteUseCase @Inject constructor(
    private val removedNoteRepository: RemovedNoteRepository
) {

    operator fun <T> invoke (): T {
        return removedNoteRepository.getListRemovedNotes()
    }
}