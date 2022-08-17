package info.sergeikolinichenko.closednotepad.repositories

import info.sergeikolinichenko.closednotepad.models.RemovedNote

interface RemovedNoteRepository {

    suspend fun addRemovedNote(removedNote: RemovedNote)

    suspend fun recoveryRemovedNote(timeStamp: Long): RemovedNote

    suspend fun deleteRemovedNote(timeStamp: Long)
}