package info.sergeikolinichenko.closednotepad.presentation.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.presentation.ui.home.progressText
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.NoteColorScheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ChecklistEditor(note: Note, colors: NoteColorScheme, viewModel: EditorViewModel) {
    val open = note.items.filter { !it.isChecked }
    val checked = note.items.filter { it.isChecked }
    var showChecked by rememberSaveable { mutableStateOf(true) }
    val focusItemId by viewModel.focusItemId.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val fromId = from.key as? String
        val toId = to.key as? String
        if (fromId != null && toId != null && open.any { it.id == toId }) viewModel.moveItem(fromId, toId)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 48.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = HEADER_KEY) {
            Row(Modifier.padding(horizontal = 8.dp)) {
                androidx.compose.foundation.layout.Column {
                    TitleField(note.title, colors, viewModel::setTitle)
                    Text(
                        progressText(note.type, checked.count { it.text.isNotBlank() }, note.items.count { it.text.isNotBlank() }),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.contentVariant
                    )
                }
            }
        }
        items(open, key = { it.id }) { item ->
            ReorderableItem(reorderState, key = item.id) { dragging ->
                Surface(
                    color = if (dragging) colors.line else Color.Transparent,
                    contentColor = colors.content
                ) {
                    ItemRow(
                        item = item,
                        colors = colors,
                        requestFocus = item.id == focusItemId,
                        onFocusHandled = viewModel::focusHandled,
                        onText = { viewModel.setItemText(item.id, it) },
                        onToggle = { viewModel.toggleItem(item.id) },
                        onNext = { viewModel.addItem(afterId = item.id) },
                        onRemove = { viewModel.removeItem(item.id) },
                        dragHandle = {
                            Icon(
                                Icons.Outlined.DragIndicator,
                                contentDescription = stringResource(R.string.reorder),
                                tint = colors.contentVariant,
                                modifier = Modifier.draggableHandle().padding(8.dp)
                            )
                        }
                    )
                }
            }
        }
        item(key = ADD_KEY) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.addItem() }
                    .padding(start = 48.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = colors.contentVariant)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.add_item), color = colors.contentVariant)
            }
        }
        if (checked.isNotEmpty()) {
            item(key = CHECKED_HEADER_KEY) {
                CheckedHeader(
                    text = stringResource(R.string.checked_count, checked.size),
                    expanded = showChecked,
                    colors = colors,
                    onClick = { showChecked = !showChecked }
                )
            }
            if (showChecked) {
                items(checked, key = { it.id }) { item ->
                    ItemRow(
                        item = item,
                        colors = colors,
                        requestFocus = false,
                        onFocusHandled = {},
                        onText = { viewModel.setItemText(item.id, it) },
                        onToggle = { viewModel.toggleItem(item.id) },
                        onNext = {},
                        onRemove = { viewModel.removeItem(item.id) },
                        dragHandle = { Spacer(Modifier.size(40.dp)) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

private const val HEADER_KEY = "header"
private const val ADD_KEY = "add"
private const val CHECKED_HEADER_KEY = "checked-header"

@Composable
fun CheckedHeader(text: String, expanded: Boolean, colors: NoteColorScheme, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = colors.contentVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.contentVariant)
    }
}

@Composable
private fun ItemRow(
    item: NoteItem,
    colors: NoteColorScheme,
    requestFocus: Boolean,
    onFocusHandled: () -> Unit,
    onText: (String) -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onRemove: () -> Unit,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            onFocusHandled()
        }
    }
    val itemLabel = item.text
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        dragHandle()
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.content,
                uncheckedColor = colors.contentVariant,
                checkmarkColor = colors.container
            ),
            modifier = Modifier.semantics { contentDescription = itemLabel }
        )
        BasicTextField(
            value = item.text,
            onValueChange = { text ->
                // Enter starts a new item instead of a new line
                if (text.contains('\n')) onNext() else onText(text)
            },
            textStyle = TextStyle(
                fontSize = 17.sp,
                color = if (item.isChecked) colors.contentVariant else colors.content,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
            ),
            cursorBrush = SolidColor(colors.content),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
                .focusRequester(focusRequester),
            decorationBox = { inner ->
                if (item.text.isEmpty()) {
                    Text(stringResource(R.string.item_hint), fontSize = 17.sp, color = colors.contentVariant.copy(alpha = 0.5f))
                }
                inner()
            }
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = stringResource(R.string.remove_item),
                tint = colors.contentVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
