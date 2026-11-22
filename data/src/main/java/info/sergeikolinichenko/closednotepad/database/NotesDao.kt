package info.sergeikolinichenko.closednotepad.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteItemDbModel
import info.sergeikolinichenko.closednotepad.dbmodels.NoteWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Transaction
    @Query("SELECT * FROM notes WHERE removedAt IS NULL")
    fun observeNotes(): Flow<List<NoteWithItems>>

    @Transaction
    @Query("SELECT * FROM notes WHERE removedAt IS NOT NULL ORDER BY removedAt DESC")
    fun observeTrash(): Flow<List<NoteWithItems>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeNote(id: Long): Flow<NoteWithItems?>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Long): NoteWithItems?

    @Insert
    suspend fun insertNote(note: NoteDbModel): Long

    // Does not touch removedAt, so saving an open note never takes it out of the trash
    @Query(
        """UPDATE notes SET type = :type, title = :title, body = :body, colorIndex = :colorIndex,
        isLocked = :isLocked, isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id"""
    )
    suspend fun updateContent(
        id: Long,
        type: String,
        title: String,
        body: String,
        colorIndex: Int,
        isLocked: Boolean,
        isPinned: Boolean,
        updatedAt: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<NoteItemDbModel>)

    @Query("DELETE FROM note_items WHERE noteId = :noteId")
    suspend fun deleteItems(noteId: Long)

    // Replaces the note content and all of its items in one transaction.
    // A note that was deleted for good is not brought back.
    @Transaction
    suspend fun saveNote(note: NoteDbModel, items: List<NoteItemDbModel>) {
        val updated = updateContent(
            note.id, note.type, note.title, note.body, note.colorIndex,
            note.isLocked, note.isPinned, note.updatedAt
        )
        if (updated == 0) return
        deleteItems(note.id)
        if (items.isNotEmpty()) insertItems(items)
    }

    @Transaction
    suspend fun createNote(note: NoteDbModel, items: List<NoteItemDbModel>): Long {
        val id = insertNote(note.copy(id = 0))
        if (items.isNotEmpty()) insertItems(items.map { it.copy(noteId = id) })
        return id
    }

    // Locked notes written before they were encrypted, in the trash too
    @Transaction
    @Query("SELECT * FROM notes WHERE isLocked = 1 AND body NOT LIKE 'sealed:v1:%'")
    suspend fun plainLockedNotes(): List<NoteWithItems>

    @Query("UPDATE notes SET body = :newBody WHERE id = :id AND body = :oldBody")
    suspend fun replaceBody(id: Long, oldBody: String, newBody: String): Int

    // Puts the encrypted content in place of the text and drops the items, all or nothing.
    // seal throws if the result doesn't open back to the same content, and then nothing is changed.
    @Transaction
    suspend fun sealPlainLockedNotes(seal: (NoteWithItems) -> String): Int {
        var count = 0
        plainLockedNotes().forEach { note ->
            if (replaceBody(note.note.id, note.note.body, seal(note)) == 1) {
                deleteItems(note.note.id)
                count++
            }
        }
        return count
    }

    @Query("UPDATE notes SET removedAt = :removedAt WHERE id IN (:ids)")
    suspend fun moveToTrash(ids: List<Long>, removedAt: Long)

    @Query("UPDATE notes SET removedAt = NULL WHERE id IN (:ids)")
    suspend fun restore(ids: List<Long>)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("DELETE FROM notes WHERE removedAt IS NOT NULL")
    suspend fun emptyTrash()

    @Query("DELETE FROM notes WHERE removedAt IS NOT NULL AND removedAt < :time")
    suspend fun deleteTrashedBefore(time: Long): Int

    @Query(
        """DELETE FROM notes WHERE removedAt IS NULL AND trim(title) = '' AND trim(body) = ''
        AND NOT EXISTS (SELECT 1 FROM note_items WHERE note_items.noteId = notes.id AND trim(note_items.text) != '')
        AND id NOT IN (:exceptIds)"""
    )
    suspend fun deleteBlankNotes(exceptIds: List<Long>)

    @Query("UPDATE notes SET isPinned = :pinned WHERE id IN (:ids)")
    suspend fun setPinned(ids: List<Long>, pinned: Boolean)

    @Query("UPDATE notes SET colorIndex = :colorIndex WHERE id IN (:ids)")
    suspend fun setColor(ids: List<Long>, colorIndex: Int)

    @Query(
        """SELECT note_items.text FROM note_items
        JOIN notes ON notes.id = note_items.noteId
        WHERE notes.type = 'SHOPPING' AND trim(note_items.text) != ''
        GROUP BY lower(note_items.text)
        ORDER BY COUNT(*) DESC, MAX(notes.updatedAt) DESC
        LIMIT :limit"""
    )
    suspend fun shoppingSuggestions(limit: Int): List<String>
}
