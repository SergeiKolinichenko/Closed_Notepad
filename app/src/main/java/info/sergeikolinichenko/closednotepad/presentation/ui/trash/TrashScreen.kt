package info.sergeikolinichenko.closednotepad.presentation.ui.trash

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.navigation.LocalAppComponent
import info.sergeikolinichenko.closednotepad.presentation.security.LocalAuthenticator
import info.sergeikolinichenko.closednotepad.presentation.ui.home.NoteCard
import info.sergeikolinichenko.closednotepad.presentation.ui.home.daysLeftText
import info.sergeikolinichenko.closednotepad.usecases.TrashPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenNote: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authenticator = LocalAuthenticator.current
    val noteLock = LocalAppComponent.current.noteLock()
    val unlocked by noteLock.unlocked.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var confirmEmpty by rememberSaveable { mutableStateOf(false) }
    val selecting = state.selected.isNotEmpty()
    val now = System.currentTimeMillis()

    BackHandler(enabled = selecting) { viewModel.clearSelection() }

    fun withUnlocked(notes: List<Note>, action: () -> Unit) {
        val locked = notes.filter { it.isLocked && it.id !in unlocked }
        if (locked.isEmpty()) action()
        else authenticator.authenticate(
            onSuccess = { noteLock.markUnlocked(locked.map { it.id }); action() },
            onError = {}
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selecting) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    navigationIcon = {
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cancel_selection))
                        }
                    },
                    title = { Text(state.selected.size.toString()) },
                    actions = {
                        IconButton(onClick = { withUnlocked(state.selectedNotes) { viewModel.restore(state.selected) } }) {
                            Icon(Icons.Outlined.RestoreFromTrash, contentDescription = stringResource(R.string.restore))
                        }
                        IconButton(onClick = { withUnlocked(state.selectedNotes) { confirmDelete = true } }) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = stringResource(R.string.delete_forever))
                        }
                    }
                )
            } else {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    title = { Text(stringResource(R.string.trash)) },
                    actions = {
                        if (state.notes.isNotEmpty()) {
                            TextButton(onClick = { withUnlocked(state.notes) { confirmEmpty = true } }) {
                                Text(stringResource(R.string.empty_trash))
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (state.loaded && state.notes.isEmpty()) {
            Column(
                Modifier.padding(padding).fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.trash_is_empty), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                item(key = "hint") {
                    Box(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                        Text(
                            if (state.daysBeforeDeletion > 0)
                                pluralStringResource(R.plurals.trash_hint_days, state.daysBeforeDeletion, state.daysBeforeDeletion)
                            else stringResource(R.string.trash_hint_forever),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(state.notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        selected = note.id in state.selected,
                        compact = true,
                        onClick = {
                            if (selecting) viewModel.toggleSelection(note.id)
                            else withUnlocked(listOf(note)) { onOpenNote(note.id) }
                        },
                        onLongClick = { viewModel.toggleSelection(note.id) },
                        dateOverride = daysLeftText(
                            TrashPolicy.daysLeft(note.removedAt ?: now, now, state.daysBeforeDeletion)
                        ),
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            text = pluralStringResource(R.plurals.confirm_delete_forever, state.selected.size, state.selected.size),
            confirm = stringResource(R.string.delete_forever),
            onConfirm = { confirmDelete = false; viewModel.deleteForever(state.selected) },
            onDismiss = { confirmDelete = false }
        )
    }
    if (confirmEmpty) {
        ConfirmDialog(
            text = stringResource(R.string.confirm_empty_trash),
            confirm = stringResource(R.string.empty_trash),
            onConfirm = { confirmEmpty = false; viewModel.emptyTrash() },
            onDismiss = { confirmEmpty = false }
        )
    }
}

@Composable
fun ConfirmDialog(text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
