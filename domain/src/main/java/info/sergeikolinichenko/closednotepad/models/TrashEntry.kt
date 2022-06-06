package info.sergeikolinichenko.closednotepad.models

data class TrashEntry(
    val timeStamp: Long,
    val timeAddEntry: Long,
    val titleEntry: String,
    val itselfEntry: String,
    val colorIndex: Int
)
