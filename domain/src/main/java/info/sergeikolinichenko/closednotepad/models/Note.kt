package info.sergeikolinichenko.closednotepad.models

data class Note(
    val timeStamp: Long,
    val titleNote: String,
    val itselfNote: String,
    val colorIndex: Int,
    val isLocked: Boolean,
    val isSelected: Boolean
)
