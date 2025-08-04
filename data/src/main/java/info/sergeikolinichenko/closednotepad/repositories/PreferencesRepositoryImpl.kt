package info.sergeikolinichenko.closednotepad.repositories

import android.content.SharedPreferences
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val sharPref: SharedPreferences
) : PreferencesRepository {

    override fun setPrefOrderNoteList(order: String) {
        sharPref.edit().putString(ORDER_VIEW_NOTE_LIST, order).apply()
    }

    override fun getPrefOrderNoteList() =
        sharPref.getString(ORDER_VIEW_NOTE_LIST, ERROR_GET_STRING)
            ?: throw RuntimeException("preferences ORDER_VIEW_NOTE_LIST isn't exist")


    override fun setPrefColorIndex(index: Int) {
        sharPref.edit().putInt(DEFAULT_COLOR_INDEX, index).apply()
    }

    override fun getPrefColorIndex() =
        sharPref.getInt(DEFAULT_COLOR_INDEX, ERROR_GET_INT)

    override fun getPrefAutoDelReNote() =
        sharPref.getInt(DAYS_BEFORE_DELETION, ERROR_GET_INT)

    override fun setPrefAutoDelReNote(days: Int) {
        sharPref.edit().putInt(DAYS_BEFORE_DELETION, days).apply()
    }

    companion object {
        private const val DEFAULT_COLOR_INDEX = "default_color_index"
        private const val ORDER_VIEW_NOTE_LIST = "order_view_note_list"
        private const val DAYS_BEFORE_DELETION = "days_before_deletion"

        const val ERROR_GET_INT = -1
        const val ERROR_GET_STRING = "error_shar_pref"

    }
}