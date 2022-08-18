package info.sergeikolinichenko.closednotepad.usecases.trashcan

import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository

class GetListRemovedNoteUseCase(
    private val removedNoteRepository: RemovedNoteRepository
) {

    operator fun <T> invoke (): T {
        return removedNoteRepository.getListRemovedNotes()
    }
}