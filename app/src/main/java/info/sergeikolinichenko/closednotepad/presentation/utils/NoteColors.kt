package info.sergeikolinichenko.closednotepad.presentation.utils

import info.sergeikolinichenko.closednotepad.R

object NoteColors {
    
    const val LIGHT_COLOR = 0
    const val DARK_COLOR = 1

    const val PINK = 0
    const val PURPLE = 1
    const val INDIGO = 2
    const val GREEN = 3
    const val ORANGE = 4
    const val BROWN = 5
    const val GRAY = 6
    const val BLUE_GRAY = 7

    
    val noteColor = arrayOf(
        intArrayOf(
            R.color.pink_100,
            R.color.purple_100,
            R.color.indigo_100,
            R.color.green_100,
            R.color.orange_100,
            R.color.brown_100,
            R.color.gray_100,
            R.color.blue_gray_100
        ),
        intArrayOf(
            R.color.pink_900,
            R.color.purple_900,
            R.color.indigo_900,
            R.color.green_900,
            R.color.orange_900,
            R.color.brown_900,
            R.color.gray_900,
            R.color.blue_gray_900
        )
    )
}