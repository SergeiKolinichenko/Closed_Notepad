package info.sergeikolinichenko.closednotepad.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.presentation.navigation.AppNavHost
import info.sergeikolinichenko.closednotepad.presentation.navigation.LocalAppComponent
import info.sergeikolinichenko.closednotepad.presentation.security.Authenticator
import info.sergeikolinichenko.closednotepad.presentation.shortcuts.NewNoteIntents
import info.sergeikolinichenko.closednotepad.presentation.security.LocalAuthenticator
import info.sergeikolinichenko.closednotepad.presentation.security.VaultDialogs
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.ClosedNotepadTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

// FragmentActivity is needed by BiometricPrompt
class MainActivity : FragmentActivity() {

    private val component by lazy { (application as NotesApp).component }
    private val openNoteRequests = Channel<Long>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val authenticator = Authenticator(this, component.notesVault())
        setContent {
            CompositionLocalProvider(
                LocalAppComponent provides component,
                LocalAuthenticator provides authenticator
            ) {
                ClosedNotepadTheme {
                    AppNavHost(component = component, openNoteRequests = openNoteRequests.receiveAsFlow())
                    VaultDialogs(authenticator)
                }
            }
        }
        // After a rotation the shared or new note is already open
        if (savedInstanceState == null) handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val newNoteType = NewNoteIntents.typeOf(intent)
        if (newNoteType != null) createNote(newNoteType) else handleSharedText(intent)
    }

    // "New note" from the widget, the quick settings tile or a launcher shortcut
    private fun createNote(type: NoteType) {
        setIntent(Intent(this, MainActivity::class.java))
        lifecycleScope.launch {
            val note = Note.blank(
                type = type,
                colorIndex = component.preferencesRepository().current().defaultColorIndex,
                now = System.currentTimeMillis(),
                newItemId = { UUID.randomUUID().toString() }
            )
            openNoteRequests.send(component.notesRepository().createNote(note))
        }
    }

    // Text shared from another app becomes a new text note
    private fun handleSharedText(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty()
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)?.trim().orEmpty()
        if (text.isEmpty() && subject.isEmpty()) return
        // Handled once, so it is not imported again when the activity is recreated
        setIntent(Intent(this, MainActivity::class.java))
        lifecycleScope.launch {
            val now = System.currentTimeMillis()
            val id = component.notesRepository().createNote(
                Note(
                    type = NoteType.TEXT,
                    title = subject,
                    body = text,
                    colorIndex = component.preferencesRepository().current().defaultColorIndex,
                    createdAt = now,
                    updatedAt = now
                )
            )
            openNoteRequests.send(id)
        }
    }
}
