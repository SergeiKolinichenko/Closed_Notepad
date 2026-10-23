package info.sergeikolinichenko.closednotepad.security

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.repositories.NotesRepositoryImpl
import info.sergeikolinichenko.closednotepad.utils.NoteMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LockedNotesTest {

    private lateinit var db: AppDatabase
    private lateinit var deviceKey: FakeDeviceKey
    private lateinit var vault: NotesVault
    private lateinit var repository: NotesRepositoryImpl
    private lateinit var encryption: LockedNotesEncryption

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        deviceKey = FakeDeviceKey()
        vault = TestVaults.vault(deviceKey)
        repository = NotesRepositoryImpl(NoteMapper(), db.notesDao(), vault)
        encryption = LockedNotesEncryption(db, vault, NoteMapper())
    }

    @After
    fun tearDown() = db.close()

    private val secretText = Note(
        type = NoteType.TEXT, title = "Bank", body = "PIN 4321", isLocked = true,
        colorIndex = 6, createdAt = 1, updatedAt = 2
    )

    private val secretList = Note(
        type = NoteType.SHOPPING, title = "Gifts", isLocked = true, colorIndex = 1, createdAt = 3, updatedAt = 4,
        items = listOf(
            NoteItem(id = "a", text = "Ring", quantity = "1", section = "Jewelry", price = 999.5),
            NoteItem(id = "b", text = "Flowers", isChecked = true)
        )
    )

    private val plain = Note(type = NoteType.TEXT, title = "Plain", body = "Open text", colorIndex = 0, createdAt = 5, updatedAt = 5)

    // Every text value stored in the database, to make sure no secret is left in it
    private fun storedTexts(): String {
        val values = mutableListOf<String>()
        listOf("SELECT title, body FROM notes", "SELECT text, quantity, section FROM note_items").forEach { sql ->
            db.openHelper.readableDatabase.query(sql).use { cursor ->
                while (cursor.moveToNext()) for (i in 0 until cursor.columnCount) values += cursor.getString(i).orEmpty()
            }
        }
        return values.joinToString("\n")
    }

    private fun content(note: Note) = Triple(note.title, note.body, note.items)

    @Test
    fun existingLockedNotesAreEncryptedWithoutLoss() = runTest {
        // Written by the previous version: plain text
        val textId = repository.createNote(secretText)
        val listId = repository.createNote(secretList)
        val trashedId = repository.createNote(secretText.copy(title = "Old", body = "Old secret"))
        repository.moveToTrash(listOf(trashedId), removedAt = 10)
        val plainId = repository.createNote(plain)
        val before = listOf(textId, listId, trashedId, plainId).associateWith { content(repository.getNote(it)!!) }
        assertTrue(storedTexts().contains("PIN 4321"))

        vault.setUp("password".toCharArray())
        assertEquals(3, encryption.encryptPlainLockedNotes())

        val stored = storedTexts()
        listOf("PIN 4321", "Ring", "Jewelry", "Flowers", "Old secret").forEach { assertFalse(it, stored.contains(it)) }
        // Titles stay readable, so search by title keeps working
        assertTrue(stored.contains("Bank") && stored.contains("Gifts"))
        assertTrue(stored.contains("Open text"))
        before.forEach { (id, expected) -> assertEquals(expected, content(repository.getNote(id)!!)) }

        // Running again finds nothing to do
        assertEquals(0, encryption.encryptPlainLockedNotes())
    }

    @Test
    fun lockedVaultGivesSealedNotesThatAreNeverOverwritten() = runTest {
        vault.setUp("password".toCharArray())
        val id = repository.createNote(secretText)
        vault.lock()

        val sealed = repository.getNote(id)!!
        assertTrue(sealed.isSealed)
        assertEquals("", sealed.body)
        assertFalse(sealed.isBlank)
        assertEquals("Bank", repository.observeNotes().first().single().title)

        // An editor that saves the sealed note must not wipe the content
        repository.saveNote(sealed.copy(title = "Bank 2"))
        assertTrue(vault.unlockWithPassword("password".toCharArray()))
        assertEquals("PIN 4321", repository.getNote(id)!!.body)
    }

    @Test
    fun lockedNoteCanBeSavedAfterTheAppWentToTheBackground() = runTest {
        vault.setUp("password".toCharArray())
        val id = repository.createNote(secretList)
        val open = repository.getNote(id)!!
        vault.lock()

        repository.saveNote(open.copy(items = open.items + NoteItem(id = "c", text = "Card")))

        assertTrue(vault.unlockWithPassword("password".toCharArray()))
        assertEquals(listOf("Ring", "Flowers", "Card"), repository.getNote(id)!!.items.map { it.text })
        assertTrue(repository.shoppingSuggestions(10).isEmpty())
    }

    @Test
    fun unlockingANoteStoresItInPlainTextAgain() = runTest {
        vault.setUp("password".toCharArray())
        val id = repository.createNote(secretList)
        repository.saveNote(repository.getNote(id)!!.copy(isLocked = false))

        assertTrue(storedTexts().contains("Ring"))
        vault.lock()
        assertEquals(secretList.items, repository.getNote(id)!!.items)
    }

    @Test
    fun wrongPasswordDoesNotOpen() {
        vault.setUp("password".toCharArray())
        vault.lock()
        assertFalse(vault.unlockWithPassword("Password".toCharArray()))
        assertFalse(vault.unlocked.value)
        assertTrue(vault.unlockWithPassword("password".toCharArray()))
    }

    @Test
    fun fingerprintOpensAfterItWasEnabled() = runTest {
        vault.setUp("password".toCharArray())
        val id = repository.createNote(secretText)
        assertTrue(vault.needsDeviceUnlock())
        deviceKey.confirmed = true
        assertTrue(vault.enableDeviceUnlock())
        vault.lock()

        assertTrue(vault.unlockWithDevice())
        assertEquals("PIN 4321", repository.getNote(id)!!.body)
    }

    @Test
    fun passwordStillOpensWhenTheKeystoreKeyIsGone() = runTest {
        vault.setUp("password".toCharArray())
        val id = repository.createNote(secretText)
        deviceKey.confirmed = true
        vault.enableDeviceUnlock()
        vault.lock()
        deviceKey.lose()

        assertFalse(vault.unlockWithDevice())
        assertTrue(vault.needsDeviceUnlock())
        assertTrue(vault.unlockWithPassword("password".toCharArray()))
        assertEquals("PIN 4321", repository.getNote(id)!!.body)
        assertTrue(vault.enableDeviceUnlock())
    }

    @Test
    fun changedPasswordReplacesTheOldOne() {
        vault.setUp("password".toCharArray())
        assertTrue(vault.changePassword("new password".toCharArray()))
        vault.lock()
        assertFalse(vault.unlockWithPassword("password".toCharArray()))
        assertTrue(vault.unlockWithPassword("new password".toCharArray()))
    }

    @Test
    fun vaultSurvivesARestartOfTheApp() = runTest {
        vault.setUp("password".toCharArray())
        val id = repository.createNote(secretText)
        val prefs = ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("test_vault", android.content.Context.MODE_PRIVATE)
        val restarted = NotesVault(prefs, deviceKey, TestVaults.ITERATIONS)
        val repositoryAfterRestart = NotesRepositoryImpl(NoteMapper(), db.notesDao(), restarted)

        assertTrue(restarted.isSetUp.value)
        assertTrue(repositoryAfterRestart.getNote(id)!!.isSealed)
        assertTrue(restarted.unlockWithPassword("password".toCharArray()))
        assertEquals("PIN 4321", repositoryAfterRestart.getNote(id)!!.body)
    }
}
