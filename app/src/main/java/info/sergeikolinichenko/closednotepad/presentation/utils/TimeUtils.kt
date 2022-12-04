package info.sergeikolinichenko.closednotepad.presentation.utils

import java.text.SimpleDateFormat
import java.util.*

class TimeUtils {
    companion object {
        fun getDate(timeStamp: Long): String {
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            return dateFormat.format(timeStamp)
        }

        fun getTime(timeStamp: Long): String {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return timeFormat.format(timeStamp)
        }

        fun getFullDate(timeStamp: Long): String {
            val fullDateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
            return fullDateFormat.format(timeStamp)
        }
    }
}