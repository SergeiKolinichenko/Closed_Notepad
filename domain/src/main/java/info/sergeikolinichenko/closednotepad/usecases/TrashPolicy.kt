package info.sergeikolinichenko.closednotepad.usecases

object TrashPolicy {

    const val DAY_MILLIS = 24L * 60 * 60 * 1000

    // Notes removed before this moment are deleted for good; null means keep forever
    fun deleteBefore(now: Long, daysBeforeDeletion: Int): Long? =
        if (daysBeforeDeletion > 0) now - daysBeforeDeletion * DAY_MILLIS else null

    // Whole days left before the note is deleted; null means it is kept forever
    fun daysLeft(removedAt: Long, now: Long, daysBeforeDeletion: Int): Int? {
        if (daysBeforeDeletion <= 0) return null
        val left = removedAt + daysBeforeDeletion * DAY_MILLIS - now
        return if (left <= 0) 0 else ((left + DAY_MILLIS - 1) / DAY_MILLIS).toInt()
    }
}
