package info.sergeikolinichenko.closednotepad.presentation.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.ui.common.formatDateTime
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.NoteColorScheme
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.SansFamily

private val LINE_HEIGHT = 28.sp

@Composable
fun TextEditor(
    note: Note,
    colors: NoteColorScheme,
    onTitle: (String) -> Unit,
    onBody: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        TitleField(note.title, colors, onTitle)
        Text(
            formatDateTime(note.updatedAt),
            style = MaterialTheme.typography.labelMedium,
            color = colors.contentVariant
        )
        Spacer(Modifier.height(8.dp))
        LinedTextField(note.body, colors, onBody)
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun TitleField(title: String, colors: NoteColorScheme, onTitle: (String) -> Unit, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.headlineSmall.copy(color = colors.content, fontWeight = FontWeight.SemiBold)
    BasicTextField(
        value = title,
        onValueChange = onTitle,
        textStyle = style,
        cursorBrush = SolidColor(colors.content),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        decorationBox = { inner ->
            if (title.isEmpty()) {
                Text(stringResource(R.string.title_hint), style = style, color = colors.contentVariant.copy(alpha = 0.5f))
            }
            inner()
        }
    )
}

// Text on lined paper, like the notepad of version 2.x
@Composable
private fun LinedTextField(text: String, colors: NoteColorScheme, onText: (String) -> Unit) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val lineHeightPx = with(LocalDensity.current) { LINE_HEIGHT.toPx() }
    val style = TextStyle(fontFamily = SansFamily, fontSize = 17.sp, lineHeight = LINE_HEIGHT, color = colors.content)
    BasicTextField(
        value = text,
        onValueChange = onText,
        textStyle = style,
        cursorBrush = SolidColor(colors.content),
        onTextLayout = { layout = it },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp)
            .drawBehind {
                val textLines = layout?.lineCount ?: 0
                val count = maxOf(textLines, (size.height / lineHeightPx).toInt())
                for (i in 0 until count) {
                    val y = if (i < textLines) layout!!.getLineBottom(i) else (i + 1) * lineHeightPx
                    drawLine(colors.line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            },
        decorationBox = { inner ->
            if (text.isEmpty()) {
                Text(stringResource(R.string.text_hint), style = style, color = colors.contentVariant.copy(alpha = 0.5f))
            }
            inner()
        }
    )
}
