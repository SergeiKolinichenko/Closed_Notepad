package info.sergeikolinichenko.closednotepad.repositories

import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote

interface RemovedNoteRepository {

    fun <T> getListRemovedNotes(): T

    suspend fun addRemovedNote(removedNote: RemovedNote)

    suspend fun recoveryRemovedNote(removedNote: RemovedNote): Note

    suspend fun deleteRemovedNote(timeStamp: Long)
}