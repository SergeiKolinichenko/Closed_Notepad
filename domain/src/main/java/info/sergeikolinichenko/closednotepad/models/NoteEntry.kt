package info.sergeikolinichenko.closednotepad.models

data class NoteEntry(
    val timeStamp: Long,
    val timeAddEntry: Long,
    val timeEditEntry: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int
)
