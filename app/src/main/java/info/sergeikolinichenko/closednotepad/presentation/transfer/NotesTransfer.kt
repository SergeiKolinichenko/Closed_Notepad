package info.sergeikolinichenko.closednotepad.presentation.transfer

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import info.sergeikolinichenko.closednotepad.usecases.NoteMarkdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

// Copies notes to and from Markdown files in a folder the user picks. The app has no
// internet access, so files are the way to move notes to another phone or app.
class NotesTransfer @Inject constructor(
    private val application: Application,
    private val notesRepository: NotesRepository,
    private val preferencesRepository: PreferencesRepository
) {

    // Writes every note (not the trash) to a new folder inside the picked one; returns how many
    suspend fun export(treeUri: Uri, includeLocked: Boolean): Int = withContext(Dispatchers.IO) {
        val notes = notesRepository.observeNotes().first()
            // A sealed note can't be read now; it is never written out empty
            .filter { !it.isBlank && !it.isSealed && (includeLocked || !it.isLocked) }
        val resolver = application.contentResolver
        val parent = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        val stamp = SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.US).format(Date())
        val folder = DocumentsContract.createDocument(
            resolver, parent, DocumentsContract.Document.MIME_TYPE_DIR, "$FOLDER_NAME $stamp"
        ) ?: error("Can't create a folder in $treeUri")
        notes.forEach { note ->
            // The provider adds " (1)" to names that are already taken
            val file = DocumentsContract.createDocument(resolver, folder, MIME_MARKDOWN, NoteMarkdown.fileName(note))
                ?: error("Can't create a file for note ${note.id}")
            resolver.openOutputStream(file)?.bufferedWriter()?.use { it.write(NoteMarkdown.toMarkdown(note)) }
                ?: error("Can't write $file")
        }
        notes.size
    }

    // Adds a note for every readable text file, skipping notes the app already has; returns how many
    suspend fun import(uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val known = notesRepository.observeNotes().first().toMutableList()
        val color = preferencesRepository.current().defaultColorIndex
        val now = System.currentTimeMillis()
        var imported = 0
        uris.forEach { uri ->
            val text = readText(uri) ?: return@forEach
            val note = NoteMarkdown.fromMarkdown(text, color, now) { UUID.randomUUID().toString() }
            if (note.isBlank || known.any { NoteMarkdown.sameContent(it, note) }) return@forEach
            notesRepository.createNote(note)
            known += note
            imported++
        }
        imported
    }

    private fun readText(uri: Uri): String? = runCatching {
        application.contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.readNBytesCompat(MAX_FILE_SIZE + 1)
            // Too big or binary: not a note
            if (bytes.size > MAX_FILE_SIZE || bytes.contains(0.toByte())) null else String(bytes, Charsets.UTF_8)
        }
    }.getOrNull()

    private fun InputStream.readNBytesCompat(limit: Int): ByteArray {
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(8 * 1024)
        while (buffer.size() < limit) {
            val read = read(chunk, 0, minOf(chunk.size, limit - buffer.size()))
            if (read < 0) break
            buffer.write(chunk, 0, read)
        }
        return buffer.toByteArray()
    }

    companion object {
        const val FOLDER_NAME = "Closed Notepad"
        const val MIME_MARKDOWN = "text/markdown"
        private const val MAX_FILE_SIZE = 1024 * 1024

        // .md files often come without a text type, so the picker shows any file
        val IMPORT_MIME_TYPES = arrayOf("text/*", "application/octet-stream")
    }
}
