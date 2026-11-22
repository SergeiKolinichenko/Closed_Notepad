package info.sergeikolinichenko.closednotepad.security

import android.util.Log
import info.sergeikolinichenko.closednotepad.database.AppDatabase
import info.sergeikolinichenko.closednotepad.utils.NoteMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Encrypts locked notes that were written before the vault was set up (all of them on the
// first run after the update). Each note is checked to open back to exactly the same content
// before anything is written, and all notes are written in one transaction, so a failure
// leaves the database as it was. Then the file is rebuilt, so no old text stays in free pages.
class LockedNotesEncryption(
    private val database: AppDatabase,
    private val vault: NotesVault,
    private val mapper: NoteMapper
) {

    private val mutex = Mutex()

    // Needs the vault unlocked, to check every note opens; returns how many notes were encrypted
    suspend fun encryptPlainLockedNotes(): Int = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (!vault.unlocked.value) return@withContext 0
            val count = database.notesDao().sealPlainLockedNotes { model ->
                val original = mapper.mapDbModelToEntity(model)
                val sealed = vault.seal(mapper.sealedContentJson(original))
                val reopened = vault.open(sealed)?.let(SealedContent::fromJson)
                check(reopened == SealedContent(original.body, original.items)) {
                    "Note ${model.note.id} doesn't open back to the same content"
                }
                sealed
            }
            if (count > 0) wipeFreePages()
            count
        }
    }

    private fun wipeFreePages() {
        runCatching {
            val db = database.openHelper.writableDatabase
            db.execSQL("VACUUM")
            db.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
        }.onFailure { Log.w(TAG, "Can't compact the database", it) }
    }

    companion object {
        private const val TAG = "LockedNotesEncryption"
    }
}
