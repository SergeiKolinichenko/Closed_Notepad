package info.sergeikolinichenko.closednotepad.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.repositories.NotesRepositoryImpl
import info.sergeikolinichenko.closednotepad.security.TestVaults
import info.sergeikolinichenko.closednotepad.utils.NoteMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2_keepsNotesAndTrash() = runTest {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO notes (timeStamp, titleNote, itselfNote, colorIndex, isLocked) " +
                    "VALUES (1000, 'Shopping', 'Milk and bread', 3, 0)"
            )
            execSQL(
                "INSERT INTO notes (timeStamp, titleNote, itselfNote, colorIndex, isLocked) " +
                    "VALUES (2000, 'Wi-Fi', 'secret', 6, 1)"
            )
            execSQL(
                "INSERT INTO removed_notes (timeStamp, timeRemove, titleEntry, itselfEntry, colorIndex, isLocked) " +
                    "VALUES (500, 3000, 'Old', 'Old text', 1, 0)"
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2).close()

        val db = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
            TEST_DB
        ).addMigrations(MIGRATION_1_2).allowMainThreadQueries().build()
        val repository = NotesRepositoryImpl(NoteMapper(), db.notesDao(), TestVaults.vault())

        val notes = repository.observeNotes().first().sortedBy { it.createdAt }
        assertEquals(listOf("Shopping", "Wi-Fi"), notes.map { it.title })
        assertEquals("Milk and bread", notes[0].body)
        assertEquals(NoteType.TEXT, notes[0].type)
        assertEquals(3, notes[0].colorIndex)
        assertEquals(true, notes[1].isLocked)
        assertNull(notes[0].removedAt)

        val trash = repository.observeTrash().first()
        assertEquals(1, trash.size)
        assertEquals("Old", trash[0].title)
        assertEquals(3000L, trash[0].removedAt)
        assertEquals(500L, trash[0].createdAt)
        db.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
