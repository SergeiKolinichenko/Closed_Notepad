package info.sergeikolinichenko.closednotepad.presentation.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.days

class TimeUtils {
    companion object{
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

        fun getDiffDays(timeStamp: Long): Int {
            val oneDay = 1000 * 60 * 60 * 24
            val result = ((Date().time - timeStamp) / oneDay)+1
            return result.toInt()
        }
    }
}