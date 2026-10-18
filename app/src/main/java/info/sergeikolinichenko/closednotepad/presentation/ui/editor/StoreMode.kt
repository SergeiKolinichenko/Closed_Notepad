package info.sergeikolinichenko.closednotepad.presentation.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.presentation.ui.common.ProgressLine
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.NoteColorScheme
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.noteColorScheme

// Big rows for one-handed use in a shop; the screen stays on
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreMode(viewModel: EditorViewModel, note: Note, onClose: () -> Unit) {
    val colors = noteColorScheme(note.colorIndex)
    val items = note.items.filter { it.text.isNotBlank() }
    val open = items.filter { !it.isChecked }
    val bought = items.filter { it.isChecked }
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    BackHandler(onBack = onClose)

    Scaffold(
        containerColor = colors.container,
        contentColor = colors.content,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.container,
                    titleContentColor = colors.content,
                    navigationIconContentColor = colors.content
                ),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close_store_mode))
                    }
                },
                title = { Text(stringResource(R.string.store_mode)) }
            )
        },
        bottomBar = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = colors.content, contentColor = colors.container),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(stringResource(R.string.finish_shopping), modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            item(key = "progress") {
                Column {
                    Text(
                        "${bought.size} / ${items.size}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.store_mode_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.contentVariant
                    )
                    Spacer(Modifier.size(8.dp))
                    ProgressLine(if (items.isEmpty()) 0f else bought.size / items.size.toFloat(), colors.accent)
                }
            }
            items(open + bought, key = { it.id }) { item ->
                SwipeRow(item, colors, onToggle = { viewModel.toggleItem(item.id) }, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun SwipeRow(item: NoteItem, colors: NoteColorScheme, onToggle: () -> Unit, modifier: Modifier) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) onToggle()
            false
        }
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromEndToStart = false,
        modifier = modifier,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(colors.accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.padding(start = 20.dp))
            }
        }
    ) {
        Surface(
            onClick = onToggle,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (item.isChecked) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = stringResource(if (item.isChecked) R.string.bought else R.string.not_bought),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.text,
                        fontSize = 20.sp,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                    )
                    if (item.section.isNotBlank()) {
                        Text(item.section, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (item.quantity.isNotBlank()) {
                    Text(item.quantity, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
