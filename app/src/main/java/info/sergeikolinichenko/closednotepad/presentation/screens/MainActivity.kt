package info.sergeikolinichenko.closednotepad.presentation.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import info.sergeikolinichenko.closednotepad.R

class MainActivity : AppCompatActivity(), NoteListFragment.FinishApp {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        launchNoteListFragment()
    }

    private fun launchNoteListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container,
            NoteListFragment.newInstance())
            .addToBackStack(
                NoteListFragment.NAME
            )
            .commit()
    }

    override fun finishApp() {
        supportFragmentManager.popBackStack()
        finish()
    }

}