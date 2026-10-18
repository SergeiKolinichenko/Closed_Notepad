package info.sergeikolinichenko.closednotepad.repositories

import info.sergeikolinichenko.closednotepad.models.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    // Notes that are not in the trash
    fun observeNotes(): Flow<List<Note>>

    // Notes in the trash, newest removal first
    fun observeTrash(): Flow<List<Note>>

    fun observeNote(id: Long): Flow<Note?>

    suspend fun getNote(id: Long): Note?

    // Inserts a new note and returns its id
    suspend fun createNote(note: Note): Long

    // Saves the note together with its items
    suspend fun saveNote(note: Note)

    suspend fun moveToTrash(ids: Collection<Long>, removedAt: Long)

    suspend fun restore(ids: Collection<Long>)

    suspend fun deleteForever(ids: Collection<Long>)

    suspend fun emptyTrash()

    // Deletes notes that were moved to the trash before the given time
    suspend fun deleteTrashedBefore(time: Long): Int

    // Deletes notes without a title, text and items, except the given ones
    suspend fun deleteBlankNotes(exceptIds: Collection<Long> = emptyList())

    suspend fun setPinned(ids: Collection<Long>, pinned: Boolean)

    suspend fun setColor(ids: Collection<Long>, colorIndex: Int)

    // Item names used in shopping lists, most frequent first
    suspend fun shoppingSuggestions(limit: Int): List<String>
}
