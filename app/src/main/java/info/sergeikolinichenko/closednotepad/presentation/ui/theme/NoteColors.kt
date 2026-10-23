package info.sergeikolinichenko.closednotepad.presentation.ui.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import info.sergeikolinichenko.closednotepad.R

// The eight note colors keep the indexes of version 2.x, now as muted tinted paper
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
        NoteColor(R.string.color_pink, Color(0xFFF2DDD7), Color(0xFF45302C)),
        NoteColor(R.string.color_purple, Color(0xFFE5DFEE), Color(0xFF36304A)),
        NoteColor(R.string.color_indigo, Color(0xFFDAE3EC), Color(0xFF28344A)),
        NoteColor(R.string.color_green, Color(0xFFDEE6D6), Color(0xFF2E3A2C)),
        NoteColor(R.string.color_orange, Color(0xFFF4E1CD), Color(0xFF45362A)),
        NoteColor(R.string.color_brown, Color(0xFFEAE1D3), Color(0xFF3D352B)),
        NoteColor(R.string.color_gray, Color(0xFFFFFDF8), Color(0xFF262422)),
        NoteColor(R.string.color_blue_gray, Color(0xFFDFE5E5), Color(0xFF2B3335))
    )

    fun get(index: Int): NoteColor = all.getOrElse(index) { all[6] }

    fun scheme(index: Int, dark: Boolean): NoteColorScheme {
        val color = get(index)
        return if (dark) {
            NoteColorScheme(
                container = color.dark,
                content = color.light,
                contentVariant = color.light.copy(alpha = 0.8f),
                line = color.light.copy(alpha = 0.16f),
                accent = color.light
            )
        } else {
            NoteColorScheme(
                container = color.light,
                content = color.dark,
                contentVariant = color.dark.copy(alpha = 0.8f),
                line = color.dark.copy(alpha = 0.16f),
                accent = color.dark
            )
        }
    }
}

@Composable
@ReadOnlyComposable
fun noteColorScheme(index: Int): NoteColorScheme = NoteColors.scheme(index, isSystemInDarkTheme())
