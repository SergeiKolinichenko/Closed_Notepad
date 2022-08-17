package info.sergeikolinichenko.closednotepad.repositories

import android.app.Application
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.utils.RemovedNoteMapper

class RemovedNoteRepositoryImpl(application: Application) : RemovedNoteRepository {

    private val notesDao = AppDatabase.getInstance(application).noteEntriesDao()
    private val mapper = RemovedNoteMapper()

    override suspend fun addRemovedNote(removedNote: RemovedNote) {
        notesDao.addRemovedNote(mapper.mapEntityToDbModel(removedNote))
    }

    override suspend fun recoveryRemovedNote(timeStamp: Long): RemovedNote {
        return mapper.mapDbModelToEntity(notesDao.getRemovedNote(timeStamp))
    }

    override suspend fun deleteRemovedNote(timeStamp: Long) {
        notesDao.deleteRemovedNote(timeStamp)
    }
}