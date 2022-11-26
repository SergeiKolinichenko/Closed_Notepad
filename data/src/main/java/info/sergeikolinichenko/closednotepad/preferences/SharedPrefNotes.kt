package info.sergeikolinichenko.closednotepad.preferences

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

abstract class SharedPrefNotes {

    companion object {
        const val SHARED_PREFS_NAME = "shared_prefs_notes"
        private var INSTANCE: SharedPreferences? = null
        private val LOCK = Any()

        fun getInstance(application: Application): SharedPreferences {
            INSTANCE?.let {
                return it
            }
            synchronized(LOCK) {
                INSTANCE?.let {
                    return it
                }
                val sharPref =
                    application.applicationContext.getSharedPreferences(
                        SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                INSTANCE = sharPref
                return sharPref
            }
        }
    }
}