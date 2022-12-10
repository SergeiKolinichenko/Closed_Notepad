package info.sergeikolinichenko.closednotepad.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.presentation.screens.NoteEditFragment
import info.sergeikolinichenko.closednotepad.presentation.screens.NoteListFragment
import info.sergeikolinichenko.closednotepad.presentation.screens.NoteViewFragment

class MainActivity : AppCompatActivity(), NoteListFragment.FinishApp, NoteViewFragment.SendNoteTo {

    private var title: String = EMPTY_STRING
    private var itself: String = EMPTY_STRING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tagFrag = supportFragmentManager.findFragmentByTag(NoteListFragment.NAME)?.tag
        val intent = intent

        intent?.let {
            if (intent.action == "android.intent.action.SEND") {
                title = getNoteTitle(intent)
                itself = getNoteItself(intent)
            }
        }
        // check, if there is no fragment, then start it

        if (intent.action == "android.intent.action.SEND" && itself.isNotEmpty()) {
            launchNoteEditFragment()

        } else if (tagFrag != NoteListFragment.NAME) {
            launchNoteListFragment()

        }
    }

    private fun launchNoteListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_container,
                NoteListFragment.newInstance(), NoteListFragment.NAME
            )
            .addToBackStack(
                NoteListFragment.NAME
            )
            .commit()
    }

    private fun launchNoteEditFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.main_container,
                NoteEditFragment.newInstanceExtraAddMode(
                    noteTitle = title,
                    noteItself = itself
                )
            )
            .addToBackStack(
                NoteEditFragment.NAME
            )
            .commit()
    }

    override fun finishApp() {
        supportFragmentManager.popBackStack()
        finish()
    }

    // Forwarding a note to another app
    override fun sendNoteTO(txtTitleNote: String, txtItselfNote: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, txtTitleNote)
        intent.putExtra(Intent.EXTRA_TEXT, txtItselfNote)
        startActivity(intent)
    }

    private fun getNoteTitle(intent: Intent): String {
        return intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: EMPTY_STRING
    }

    private fun getNoteItself(intent: Intent): String {
        return intent.getStringExtra(Intent.EXTRA_TEXT) ?: EMPTY_STRING
    }

    companion object {
        private const val EMPTY_STRING = ""
    }
}