package info.sergeikolinichenko.closednotepad.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.presentation.ui.common.ProgressLine
import info.sergeikolinichenko.closednotepad.presentation.ui.common.formatShortDate
import info.sergeikolinichenko.closednotepad.presentation.ui.common.icon
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.NoteColorScheme
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.noteColorScheme
import info.sergeikolinichenko.closednotepad.usecases.NoteText

private const val MAX_PREVIEW_ITEMS = 4
private const val LOCKED_PLACEHOLDER = "••••••••"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    dateOverride: String? = null
) {
    val colors = noteColorScheme(note.colorIndex)
    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(16.dp),
        border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        if (compact) CompactContent(note, colors, dateOverride) else FullContent(note, colors, dateOverride)
    }
}

@Composable
private fun CardTitle(note: Note, colors: NoteColorScheme, maxLines: Int) {
    val title = NoteText.displayTitle(note).ifEmpty { stringResource(R.string.untitled) }
    Row(verticalAlignment = Alignment.Top) {
        if (note.type != NoteType.TEXT) {
            Icon(note.type.icon, contentDescription = null, modifier = Modifier.size(18.dp).padding(top = 1.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (note.isPinned) {
            Icon(
                Icons.Outlined.PushPin,
                contentDescription = stringResource(R.string.pinned),
                tint = colors.contentVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        if (note.isLocked) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = stringResource(R.string.locked),
                tint = colors.contentVariant,
                modifier = Modifier.padding(start = 4.dp).size(16.dp)
            )
        }
    }
}

@Composable
private fun FullContent(note: Note, colors: NoteColorScheme, dateOverride: String?) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CardTitle(note, colors, maxLines = 3)
        when {
            // The content of locked notes is never shown in lists
            note.isLocked -> Text(LOCKED_PLACEHOLDER, color = colors.contentVariant)
            note.type == NoteType.TEXT -> {
                val body = note.body.trim()
                if (body.isNotEmpty() && body != note.title.trim()) {
                    Text(
                        body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.contentVariant,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            else -> ItemsPreview(note, colors)
        }
        Text(
            dateOverride ?: formatShortDate(note.updatedAt),
            style = MaterialTheme.typography.labelSmall,
            color = colors.contentVariant
        )
    }
}

@Composable
private fun ItemsPreview(note: Note, colors: NoteColorScheme) {
    val items = note.items.filter { it.text.isNotBlank() }
    val open = items.filter { !it.isChecked }
    val shown = (open + items.filter { it.isChecked }).take(MAX_PREVIEW_ITEMS)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        shown.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (item.isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                    contentDescription = null,
                    tint = colors.contentVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    item.text + if (item.quantity.isNotBlank()) " · ${item.quantity}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.contentVariant,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (items.size > shown.size) {
            Text(
                stringResource(R.string.more_items, items.size - shown.size),
                style = MaterialTheme.typography.labelMedium,
                color = colors.contentVariant
            )
        }
        if (note.type == NoteType.SHOPPING && items.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            ProgressLine(items.count { it.isChecked } / items.size.toFloat(), colors.accent)
            Text(
                progressText(note.type, items.count { it.isChecked }, items.size),
                style = MaterialTheme.typography.labelMedium,
                color = colors.contentVariant
            )
        }
    }
}

@Composable
private fun CompactContent(note: Note, colors: NoteColorScheme, dateOverride: String?) {
    Row(
        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            CardTitle(note, colors, maxLines = 1)
            val summary = when {
                note.isLocked -> LOCKED_PLACEHOLDER
                note.type == NoteType.TEXT ->
                    note.body.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
                else -> {
                    val items = note.items.filter { it.text.isNotBlank() }
                    progressText(note.type, items.count { it.isChecked }, items.size)
                }
            }
            if (summary.isNotEmpty()) {
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.contentVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            dateOverride ?: formatShortDate(note.updatedAt),
            style = MaterialTheme.typography.labelSmall,
            color = colors.contentVariant
        )
    }
}

@Composable
fun progressText(type: NoteType, done: Int, total: Int): String = when {
    total == 0 -> stringResource(R.string.list_is_empty)
    type == NoteType.SHOPPING && done == total -> stringResource(R.string.all_bought)
    type == NoteType.SHOPPING -> stringResource(R.string.bought_of, done, total)
    done == total -> stringResource(R.string.all_done)
    else -> stringResource(R.string.done_of, done, total)
}

@Composable
fun daysLeftText(days: Int?): String = when (days) {
    null -> stringResource(R.string.kept_forever)
    0 -> stringResource(R.string.deleted_soon)
    else -> pluralStringResource(R.plurals.deleted_in_days, days, days)
}
