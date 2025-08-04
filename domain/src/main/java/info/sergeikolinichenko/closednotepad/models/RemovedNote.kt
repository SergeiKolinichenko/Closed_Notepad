package info.sergeikolinichenko.closednotepad.models

data class RemovedNote(
    val timeStamp: Long,
    val timeRemove: Long,
    val titleNote: String,
    val itselfNote: String,
    val colorIndex: Int,
    val isLocked: Boolean,
    val isSelected: Boolean
)
