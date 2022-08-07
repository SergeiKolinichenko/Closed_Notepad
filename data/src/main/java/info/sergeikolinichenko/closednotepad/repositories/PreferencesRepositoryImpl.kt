package info.sergeikolinichenko.closednotepad.repositories

import android.app.Application
import info.sergeikolinichenko.closednotepad.preferences.SharedPrefNotes

class PreferencesRepositoryImpl(application: Application): PreferencesRepository {

    private val sharPref = SharedPrefNotes.getInstance(application)

    override fun setOrderViewNoteList(order: String) {
        sharPref.edit().putString(ORDER_VIEW_NOTE_LIST, order).apply()
    }

    override fun getOrderViewNoteList() =
        sharPref.getString(ORDER_VIEW_NOTE_LIST, ERROR_GET_STRING) ?:
        throw RuntimeException("preferences ORDER_VIEW_NOTE_LIST isn't exist")


    override fun setDefColorIndex(index: Int) {
        sharPref.edit().putInt(DEFAULT_COLOR_INDEX, index).apply()
    }

    override fun getDefColorIndex() = sharPref.getInt(DEFAULT_COLOR_INDEX, ERROR_GET_INT)

    companion object{
        private const val DEFAULT_COLOR_INDEX = "default_color_index"
        private const val ORDER_VIEW_NOTE_LIST = "order_view_note_list"
        private const val ERROR_GET_INT = -1
        private const val ERROR_GET_STRING = "error_shar_pref"

    }
}