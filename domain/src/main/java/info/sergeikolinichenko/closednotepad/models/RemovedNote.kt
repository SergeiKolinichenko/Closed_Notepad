package info.sergeikolinichenko.closednotepad.models

data class RemovedNote(
    val timeStamp: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int,
    val isLocked: Boolean
)
