package info.sergeikolinichenko.closednotepad.repositories

import info.sergeikolinichenko.closednotepad.database.NotesDao
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.utils.NoteMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val mapper: NoteMapper,
    private val notesDao: NotesDao
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        notesDao.observeNotes().map { list -> list.map(mapper::mapDbModelToEntity) }

    override fun observeTrash(): Flow<List<Note>> =
        notesDao.observeTrash().map { list -> list.map(mapper::mapDbModelToEntity) }

    override fun observeNote(id: Long): Flow<Note?> =
        notesDao.observeNote(id).map { it?.let(mapper::mapDbModelToEntity) }

    override suspend fun getNote(id: Long): Note? =
        notesDao.getNote(id)?.let(mapper::mapDbModelToEntity)

    override suspend fun createNote(note: Note): Long =
        notesDao.createNote(mapper.mapEntityToDbModel(note), mapper.mapItemsToDbModels(note))

    override suspend fun saveNote(note: Note) {
        notesDao.saveNote(mapper.mapEntityToDbModel(note), mapper.mapItemsToDbModels(note))
    }

    override suspend fun moveToTrash(ids: Collection<Long>, removedAt: Long) {
        notesDao.moveToTrash(ids.toList(), removedAt)
    }

    override suspend fun restore(ids: Collection<Long>) {
        notesDao.restore(ids.toList())
    }

    override suspend fun deleteForever(ids: Collection<Long>) {
        notesDao.delete(ids.toList())
    }

    override suspend fun emptyTrash() {
        notesDao.emptyTrash()
    }

    override suspend fun deleteTrashedBefore(time: Long): Int =
        notesDao.deleteTrashedBefore(time)

    override suspend fun deleteBlankNotes(exceptIds: Collection<Long>) {
        notesDao.deleteBlankNotes(exceptIds.toList())
    }

    override suspend fun setPinned(ids: Collection<Long>, pinned: Boolean) {
        notesDao.setPinned(ids.toList(), pinned)
    }

    override suspend fun setColor(ids: Collection<Long>, colorIndex: Int) {
        notesDao.setColor(ids.toList(), colorIndex)
    }

    override suspend fun shoppingSuggestions(limit: Int): List<String> =
        notesDao.shoppingSuggestions(limit)
}
