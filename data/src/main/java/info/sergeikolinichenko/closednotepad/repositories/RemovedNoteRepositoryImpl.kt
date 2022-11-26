package info.sergeikolinichenko.closednotepad.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.sergeikolinichenko.closednotepad.database.NotesDao
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.utils.RemovedNoteMapper
import javax.inject.Inject

class RemovedNoteRepositoryImpl @Inject constructor(
    private val mapper: RemovedNoteMapper,
    private val notesDao: NotesDao
) : RemovedNoteRepository {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getListRemovedNotes(): T {
        val removedNoteList: LiveData<List<RemovedNote>> =
            Transformations.map(notesDao.getRemovedNoteList()) {
                mapper.mapListDbModelToListEntity(it)
            }
        return removedNoteList as T
    }

    override suspend fun getRemovedNote(timeStamp: Long): RemovedNote {
        return mapper.mapDbModelToEntity(notesDao.getRemovedNote(timeStamp))
    }

    override suspend fun addRemovedNote(removedNote: RemovedNote) {
        notesDao.addRemovedNote(mapper.mapEntityToDbModel(removedNote))
    }

    override suspend fun recoveryRemovedNote(removedNote: RemovedNote): Note {
        deleteRemovedNote(removedNote.timeStamp)
        return mapper.mapRemovedNoteToNote(removedNote)
    }

    override suspend fun deleteRemovedNote(timeStamp: Long) {
        notesDao.deleteRemovedNote(timeStamp)
    }
}