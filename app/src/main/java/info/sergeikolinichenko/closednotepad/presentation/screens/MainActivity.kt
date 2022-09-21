package info.sergeikolinichenko.closednotepad.presentation.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import info.sergeikolinichenko.closednotepad.R

class MainActivity : AppCompatActivity(), NoteListFragment.FinishApp, NoteViewFragment.SendNoteTo {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tagFrag = supportFragmentManager.findFragmentByTag(NoteListFragment.NAME)?.tag
        if (tagFrag != NoteListFragment.NAME) {
            launchNoteListFragment()
        }
    }

    private fun launchNoteListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container,
            NoteListFragment.newInstance(), NoteListFragment.NAME)
            .addToBackStack(
                NoteListFragment.NAME
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

}