package info.sergeikolinichenko.closednotepad.models

import java.io.Serializable

data class NoteEntry(
    val timeStamp: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int,
    val isLocked: Boolean,
    val isSelected: Boolean
)
