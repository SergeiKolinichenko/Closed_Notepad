package info.sergeikolinichenko.closednotepad.presentation.shortcuts

import android.annotation.SuppressLint
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import info.sergeikolinichenko.closednotepad.models.NoteType

// Quick settings tile that opens a new text note
@RequiresApi(Build.VERSION_CODES.N)
class NewNoteTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        // On the lock screen the phone is unlocked first, so the note is not written for anyone to read
        if (isLocked) unlockAndRun { openNewNote() } else openNewNote()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun openNewNote() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(NewNoteIntents.pendingIntent(this, NoteType.TEXT))
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(NewNoteIntents.intent(this, NoteType.TEXT))
        }
    }
}
