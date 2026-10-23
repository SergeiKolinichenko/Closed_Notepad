package info.sergeikolinichenko.closednotepad.models

enum class SortOrder(val prefValue: String) {
    UPDATED_DESC("sort_updated_descending"),
    CREATED_DESC("sort_time_stamp_descending"),
    CREATED_ASC("sort_time_stamp_ascending"),
    TITLE_ASC("sort_title_ascending"),
    TITLE_DESC("sort_title_descending"),
    COLOR("sort_palette");

    companion object {
        fun fromPref(value: String?): SortOrder =
            entries.firstOrNull { it.prefValue == value } ?: UPDATED_DESC
    }
}

enum class NotesLayout { GRID, LIST }

data class Settings(
    val sortOrder: SortOrder = SortOrder.UPDATED_DESC,
    val defaultColorIndex: Int = DEFAULT_COLOR_INDEX,
    val daysBeforeDeletion: Int = DEFAULT_DAYS_BEFORE_DELETION,
    val layout: NotesLayout = NotesLayout.GRID
) {
    companion object {
        const val DEFAULT_COLOR_INDEX = 6
        const val DEFAULT_DAYS_BEFORE_DELETION = 30
        val DAYS_BEFORE_DELETION_OPTIONS = listOf(0, 10, 30, 90, 180, 360)
    }
}
