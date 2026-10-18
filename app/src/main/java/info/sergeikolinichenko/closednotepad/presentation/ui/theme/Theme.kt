package info.sergeikolinichenko.closednotepad.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5D4037),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3E2723),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF8C7B75),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE9DFD8),
    onSecondaryContainer = Color(0xFF3E2723),
    background = Color(0xFFFBF8F4),
    onBackground = Color(0xFF2C2A28),
    surface = Color(0xFFFBF8F4),
    onSurface = Color(0xFF2C2A28),
    surfaceVariant = Color(0xFFEFE8E2),
    onSurfaceVariant = Color(0xFF5F5A56),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2ED),
    surfaceContainer = Color(0xFFF4EFE9),
    surfaceContainerHigh = Color(0xFFEFE8E2),
    surfaceContainerHighest = Color(0xFFE9E2DB),
    outline = Color(0xFFB4ADA7),
    outlineVariant = Color(0xFFD9D2CC)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD7CCC8),
    onPrimary = Color(0xFF3E2723),
    primaryContainer = Color(0xFFD7CCC8),
    onPrimaryContainer = Color(0xFF3E2723),
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF4E3B35),
    onSecondaryContainer = Color(0xFFEFDCD5),
    background = Color(0xFF1C1917),
    onBackground = Color(0xFFECE6E1),
    surface = Color(0xFF1C1917),
    onSurface = Color(0xFFECE6E1),
    surfaceVariant = Color(0xFF3A3431),
    onSurfaceVariant = Color(0xFFC9C1BB),
    surfaceContainerLowest = Color(0xFF151311),
    surfaceContainerLow = Color(0xFF221E1C),
    surfaceContainer = Color(0xFF26221F),
    surfaceContainerHigh = Color(0xFF302B28),
    surfaceContainerHighest = Color(0xFF3A3431),
    outline = Color(0xFF8E8681),
    outlineVariant = Color(0xFF4A433F)
)

@Composable
fun ClosedNotepadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
