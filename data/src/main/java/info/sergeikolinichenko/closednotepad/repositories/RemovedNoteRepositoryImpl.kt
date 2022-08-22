package info.sergeikolinichenko.closednotepad.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.utils.RemovedNoteMapper

class RemovedNoteRepositoryImpl(application: Application) : RemovedNoteRepository {

    private val notesDao = AppDatabase.getInstance(application).noteEntriesDao()
    private val mapper = RemovedNoteMapper()

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