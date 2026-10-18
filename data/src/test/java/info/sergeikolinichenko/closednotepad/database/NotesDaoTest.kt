package info.sergeikolinichenko.closednotepad.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.repositories.NotesRepositoryImpl
import info.sergeikolinichenko.closednotepad.utils.NoteMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotesDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: NotesRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = NotesRepositoryImpl(NoteMapper(), db.notesDao())
    }

    @After
    fun tearDown() = db.close()

    private fun list(vararg items: String) = Note(
        type = NoteType.SHOPPING,
        title = "Products",
        items = items.mapIndexed { i, text -> NoteItem(id = "item$i-$text", text = text) },
        colorIndex = 0,
        createdAt = 1,
        updatedAt = 1
    )

    @Test
    fun saveKeepsItemOrderAndReplacesItems() = runTest {
        val id = repository.createNote(list("Milk", "Bread", "Eggs"))
        val saved = repository.getNote(id)!!
        assertEquals(listOf("Milk", "Bread", "Eggs"), saved.items.map { it.text })

        repository.saveNote(saved.copy(items = saved.items.reversed().drop(1)))
        assertEquals(listOf("Bread", "Milk"), repository.getNote(id)!!.items.map { it.text })
    }

    @Test
    fun savingOpenNoteDoesNotRestoreItFromTrash() = runTest {
        val id = repository.createNote(list("Milk"))
        val open = repository.getNote(id)!!
        repository.moveToTrash(listOf(id), removedAt = 50)

        repository.saveNote(open.copy(title = "Edited"))

        val note = repository.getNote(id)!!
        assertEquals(50L, note.removedAt)
        assertEquals("Edited", note.title)
    }

    @Test
    fun deleteForeverRemovesItems() = runTest {
        val id = repository.createNote(list("Milk"))
        repository.deleteForever(listOf(id))
        assertNull(repository.getNote(id))
        assertTrue(repository.shoppingSuggestions(10).isEmpty())
    }

    @Test
    fun trashCleanupUsesRemovalTime() = runTest {
        val oldNote = repository.createNote(list("Old").copy(createdAt = 1))
        val freshNote = repository.createNote(list("Fresh").copy(createdAt = 1))
        repository.moveToTrash(listOf(oldNote), removedAt = 100)
        repository.moveToTrash(listOf(freshNote), removedAt = 10_000)

        assertEquals(1, repository.deleteTrashedBefore(5_000))
        assertEquals(listOf(freshNote), repository.observeTrash().first().map { it.id })
    }

    @Test
    fun blankNotesAreDeletedExceptOpenOne() = runTest {
        val blank = Note(type = NoteType.TEXT, colorIndex = 0, createdAt = 1, updatedAt = 1)
        val first = repository.createNote(blank)
        val open = repository.createNote(blank)
        val kept = repository.createNote(blank.copy(body = "text"))

        repository.deleteBlankNotes(exceptIds = listOf(open))

        assertNull(repository.getNote(first))
        assertEquals(setOf(open, kept), repository.observeNotes().first().map { it.id }.toSet())
    }

    @Test
    fun suggestionsAreMostFrequentFirst() = runTest {
        repository.createNote(list("Milk", "Bread"))
        repository.createNote(list("milk", "Eggs"))
        assertEquals("Milk", repository.shoppingSuggestions(1).single().replaceFirstChar { it.uppercase() })
    }
}
