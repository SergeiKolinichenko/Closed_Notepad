package info.sergeikolinichenko.closednotepad.repositories

import android.content.SharedPreferences
import info.sergeikolinichenko.closednotepad.models.NotesLayout
import info.sergeikolinichenko.closednotepad.models.Settings
import info.sergeikolinichenko.closednotepad.models.SortOrder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val sharPref: SharedPreferences
) : PreferencesRepository {

    override val settings: Flow<Settings> = callbackFlow {
        trySend(current())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(current()) }
        sharPref.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharPref.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override fun current(): Settings {
        val color = sharPref.getInt(DEFAULT_COLOR_INDEX, NOT_SET)
        val days = sharPref.getInt(DAYS_BEFORE_DELETION, NOT_SET)
        return Settings(
            sortOrder = SortOrder.fromPref(sharPref.getString(ORDER_VIEW_NOTE_LIST, null)),
            defaultColorIndex = if (color in 0 until COLORS_COUNT) color else Settings.DEFAULT_COLOR_INDEX,
            daysBeforeDeletion = if (days >= 0) days else Settings.DEFAULT_DAYS_BEFORE_DELETION,
            layout = NotesLayout.entries.firstOrNull { it.name == sharPref.getString(NOTES_LAYOUT, null) }
                ?: NotesLayout.GRID
        )
    }

    override fun setSortOrder(order: SortOrder) {
        sharPref.edit().putString(ORDER_VIEW_NOTE_LIST, order.prefValue).apply()
    }

    override fun setDefaultColorIndex(index: Int) {
        sharPref.edit().putInt(DEFAULT_COLOR_INDEX, index).apply()
    }

    override fun setDaysBeforeDeletion(days: Int) {
        sharPref.edit().putInt(DAYS_BEFORE_DELETION, days).apply()
    }

    override fun setLayout(layout: NotesLayout) {
        sharPref.edit().putString(NOTES_LAYOUT, layout.name).apply()
    }

    companion object {
        const val SHARED_PREFS_NAME = "shared_prefs_notes"

        // Keys are the same as in version 2.x so existing settings are kept
        private const val DEFAULT_COLOR_INDEX = "default_color_index"
        private const val ORDER_VIEW_NOTE_LIST = "order_view_note_list"
        private const val DAYS_BEFORE_DELETION = "days_before_deletion"
        private const val NOTES_LAYOUT = "notes_layout"

        private const val NOT_SET = -1
        private const val COLORS_COUNT = 8
    }
}
