package info.sergeikolinichenko.closednotepad.models

data class NoteEntry(
    val timeStamp: Long,
    val timeEditEntry: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int,
    val isLocked: Boolean
)
