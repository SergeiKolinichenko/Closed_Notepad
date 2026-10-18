package info.sergeikolinichenko.closednotepad.presentation.ui.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import info.sergeikolinichenko.closednotepad.R

// The eight note colors of version 2.x, kept under the same indexes as in the database
data class NoteColor(
    @StringRes val name: Int,
    val light: Color,
    val dark: Color
)

data class NoteColorScheme(
    val container: Color,
    val content: Color,
    val contentVariant: Color,
    val line: Color,
    val accent: Color
)

object NoteColors {

    val all = listOf(
        NoteColor(R.string.color_pink, Color(0xFFF8BBD0), Color(0xFF880E4F)),
        NoteColor(R.string.color_purple, Color(0xFFE1BEE7), Color(0xFF4A148C)),
        NoteColor(R.string.color_indigo, Color(0xFFC5CAE9), Color(0xFF1A237E)),
        NoteColor(R.string.color_green, Color(0xFFC8E6C9), Color(0xFF1B5E20)),
        NoteColor(R.string.color_orange, Color(0xFFFFCCBC), Color(0xFFBF360C)),
        NoteColor(R.string.color_brown, Color(0xFFD7CCC8), Color(0xFF3E2723)),
        NoteColor(R.string.color_gray, Color(0xFFEDEBE8), Color(0xFF2E2C2A)),
        NoteColor(R.string.color_blue_gray, Color(0xFFCFD8DC), Color(0xFF263238))
    )

    fun get(index: Int): NoteColor = all.getOrElse(index) { all[6] }

    fun scheme(index: Int, dark: Boolean): NoteColorScheme {
        val color = get(index)
        return if (dark) {
            NoteColorScheme(
                container = color.dark,
                content = color.light,
                contentVariant = color.light.copy(alpha = 0.75f),
                line = color.light.copy(alpha = 0.16f),
                accent = color.light
            )
        } else {
            NoteColorScheme(
                container = color.light,
                content = color.dark,
                contentVariant = color.dark.copy(alpha = 0.75f),
                line = color.dark.copy(alpha = 0.16f),
                accent = color.dark
            )
        }
    }
}

@Composable
@ReadOnlyComposable
fun noteColorScheme(index: Int): NoteColorScheme = NoteColors.scheme(index, isSystemInDarkTheme())
