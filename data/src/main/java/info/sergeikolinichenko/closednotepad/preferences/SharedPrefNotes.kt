package info.sergeikolinichenko.closednotepad.preferences

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

abstract class SharedPrefNotes() {

//    fun getData(name: String): Int {
//        return sharedPreferences.getInt(name, ERROR_GET_INT)
//    }
//
//    fun setData(name: String, data: Int) {
//        sharedPreferences.edit().putInt(name, data).apply()
//    }

    companion object {
        private const val SHARED_PREFS_NAME = "shared_prefs_notes"
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