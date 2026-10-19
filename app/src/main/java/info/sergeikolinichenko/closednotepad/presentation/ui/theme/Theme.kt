package info.sergeikolinichenko.closednotepad.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import info.sergeikolinichenko.closednotepad.R

// "Paper and seal": paper, ink and one accent, the sealing wax of locked notes
object Paper {
    val Light = Color(0xFFF3EFE7)
    val Sheet = Color(0xFFFFFDF8)
    val Ink = Color(0xFF22201E)
    val InkSoft = Color(0xFF6B645B)
    val Line = Color(0xFFDDD5C8)
    val Seal = Color(0xFFB5412F)
    val SealDark = Color(0xFF8E3022)
    val SealOnDark = Color(0xFFD9604A)
    val Night = Color(0xFF1A1917)
    val NightSheet = Color(0xFF262422)
    val NightInk = Color(0xFFECE6DC)
    val NightInkSoft = Color(0xFFA39B90)
    val NightLine = Color(0xFF3A3632)
}

// Literata for titles, Golos Text for everything else; both are under the SIL Open Font License
val SerifFamily = FontFamily(Font(R.font.literata_semibold, FontWeight.SemiBold))

val SansFamily = FontFamily(
    Font(R.font.golos_regular, FontWeight.Normal),
    Font(R.font.golos_semibold, FontWeight.SemiBold)
)

private val LightColors = lightColorScheme(
    primary = Paper.Ink,
    onPrimary = Paper.Sheet,
    primaryContainer = Paper.Ink,
    onPrimaryContainer = Paper.Sheet,
    secondary = Paper.InkSoft,
    onSecondary = Paper.Sheet,
    secondaryContainer = Color(0xFFE6DFD3),
    onSecondaryContainer = Paper.Ink,
    tertiary = Paper.Seal,
    onTertiary = Paper.Sheet,
    error = Paper.Seal,
    onError = Paper.Sheet,
    background = Paper.Light,
    onBackground = Paper.Ink,
    surface = Paper.Light,
    onSurface = Paper.Ink,
    surfaceVariant = Color(0xFFEAE4DA),
    onSurfaceVariant = Paper.InkSoft,
    surfaceContainerLowest = Paper.Sheet,
    surfaceContainerLow = Color(0xFFF7F3EC),
    surfaceContainer = Color(0xFFF7F3EC),
    surfaceContainerHigh = Paper.Sheet,
    surfaceContainerHighest = Color(0xFFEAE4DA),
    outline = Color(0xFFB3AA9D),
    outlineVariant = Paper.Line
)

private val DarkColors = darkColorScheme(
    primary = Paper.NightInk,
    onPrimary = Paper.Night,
    primaryContainer = Paper.NightInk,
    onPrimaryContainer = Paper.Night,
    secondary = Paper.NightInkSoft,
    onSecondary = Paper.Night,
    secondaryContainer = Color(0xFF34302C),
    onSecondaryContainer = Paper.NightInk,
    tertiary = Paper.SealOnDark,
    onTertiary = Paper.Night,
    error = Paper.SealOnDark,
    onError = Paper.Night,
    background = Paper.Night,
    onBackground = Paper.NightInk,
    surface = Paper.Night,
    onSurface = Paper.NightInk,
    surfaceVariant = Color(0xFF2E2B28),
    onSurfaceVariant = Paper.NightInkSoft,
    surfaceContainerLowest = Color(0xFF141311),
    surfaceContainerLow = Color(0xFF201E1C),
    surfaceContainer = Color(0xFF221F1D),
    surfaceContainerHigh = Paper.NightSheet,
    surfaceContainerHighest = Color(0xFF2E2B28),
    outline = Color(0xFF7D766D),
    outlineVariant = Paper.NightLine
)

private val AppTypography = Typography().run {
    fun TextStyle.serif() = copy(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold)
    fun TextStyle.sans() = copy(fontFamily = SansFamily)
    Typography(
        displayLarge = displayLarge.serif(),
        displayMedium = displayMedium.serif(),
        displaySmall = displaySmall.serif(),
        headlineLarge = headlineLarge.serif().copy(fontSize = 34.sp, lineHeight = 40.sp),
        headlineMedium = headlineMedium.serif(),
        headlineSmall = headlineSmall.serif(),
        titleLarge = titleLarge.serif(),
        titleMedium = titleMedium.sans().copy(fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.sans().copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.sans().copy(fontSize = 17.sp, lineHeight = 26.sp),
        bodyMedium = bodyMedium.sans().copy(fontSize = 15.sp, lineHeight = 22.sp),
        bodySmall = bodySmall.sans().copy(fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = labelLarge.sans().copy(fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.sans(),
        labelSmall = labelSmall.sans().copy(fontSize = 12.sp)
    )
}

// Note titles in cards and the editor
val NoteTitleStyle = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp)

// Small caps-like headers of the note groups
val GroupHeaderStyle = TextStyle(fontFamily = SansFamily, fontSize = 12.sp, letterSpacing = 1.4.sp)

@Composable
fun ClosedNotepadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
