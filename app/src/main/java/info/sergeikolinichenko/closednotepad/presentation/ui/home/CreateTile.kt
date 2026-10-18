package info.sergeikolinichenko.closednotepad.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.presentation.ui.common.icon
import info.sergeikolinichenko.closednotepad.presentation.ui.common.label
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.noteColorScheme

@Composable
fun CreateTile(type: NoteType, hint: Int, colorIndex: Int, onCreate: (NoteType) -> Unit) {
    val colors = noteColorScheme(colorIndex)
    Surface(
        onClick = { onCreate(type) },
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(type.icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(type.label), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(stringResource(hint), style = MaterialTheme.typography.bodyMedium, color = colors.contentVariant)
            }
        }
    }
}
