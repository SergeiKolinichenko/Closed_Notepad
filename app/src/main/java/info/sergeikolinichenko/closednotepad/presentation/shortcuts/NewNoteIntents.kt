package info.sergeikolinichenko.closednotepad.presentation.shortcuts

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.presentation.MainActivity

// "New note" from outside the app: the home screen widget, the quick settings tile
// and the launcher shortcuts (res/xml/shortcuts.xml uses the same action and extra)
object NewNoteIntents {

    const val ACTION_NEW_NOTE = "info.sergeikolinichenko.closednotepad.action.NEW_NOTE"
    private const val EXTRA_TYPE = "type"

    fun intent(context: Context, type: NoteType): Intent =
        Intent(context, MainActivity::class.java)
            .setAction(ACTION_NEW_NOTE)
            .putExtra(EXTRA_TYPE, type.name)
            // Opens over whatever the app was showing instead of a second copy of it
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

    fun pendingIntent(context: Context, type: NoteType): PendingIntent =
        PendingIntent.getActivity(
            context,
            type.ordinal,
            intent(context, type),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    // The note type asked for, or null when the intent is not a "new note" request
    fun typeOf(intent: Intent?): NoteType? {
        if (intent?.action != ACTION_NEW_NOTE) return null
        val name = intent.getStringExtra(EXTRA_TYPE)
        return NoteType.entries.firstOrNull { it.name == name } ?: NoteType.TEXT
    }
}
