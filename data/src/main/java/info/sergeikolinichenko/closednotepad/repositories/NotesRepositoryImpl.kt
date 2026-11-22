package info.sergeikolinichenko.closednotepad.repositories

import info.sergeikolinichenko.closednotepad.database.NotesDao
import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteItemDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteWithItems
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.security.NotesVault
import info.sergeikolinichenko.closednotepad.utils.NoteMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

// Locked notes are encrypted here on the way to the database and opened on the way back
// while the vault is unlocked. Until then they come out sealed, with empty content.
class NotesRepositoryImpl @Inject constructor(
    private val mapper: NoteMapper,
    private val notesDao: NotesDao,
    private val vault: NotesVault
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        notesDao.observeNotes().combine(vault.unlocked) { list, _ -> list.map(::toEntity) }.flowOn(Dispatchers.Default)

    override fun observeTrash(): Flow<List<Note>> =
        notesDao.observeTrash().combine(vault.unlocked) { list, _ -> list.map(::toEntity) }.flowOn(Dispatchers.Default)

    override fun observeNote(id: Long): Flow<Note?> =
        notesDao.observeNote(id).combine(vault.unlocked) { note, _ -> note?.let(::toEntity) }.flowOn(Dispatchers.Default)

    override suspend fun getNote(id: Long): Note? =
        notesDao.getNote(id)?.let(::toEntity)

    override suspend fun createNote(note: Note): Long {
        val (model, items) = toDbModels(note)
        return notesDao.createNote(model, items)
    }

    // A sealed note was never opened, so there is nothing new to write and its content stays as it is
    override suspend fun saveNote(note: Note) {
        if (note.isSealed) return
        val (model, items) = toDbModels(note)
        notesDao.saveNote(model, items)
    }

    private fun toEntity(model: NoteWithItems): Note = mapper.mapDbModelToEntity(model, vault::open)

    // Before the vault is set up locked notes are written as before; they are encrypted when it is
    private fun toDbModels(note: Note): Pair<NoteDbModel, List<NoteItemDbModel>> =
        if (note.isLocked && vault.isSetUp.value) {
            mapper.mapEntityToDbModel(note).copy(body = vault.seal(mapper.sealedContentJson(note))) to emptyList()
        } else {
            mapper.mapEntityToDbModel(note) to mapper.mapItemsToDbModels(note)
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
