package info.sergeikolinichenko.closednotepad.presentation.ui.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.presentation.navigation.LocalAppComponent
import info.sergeikolinichenko.closednotepad.presentation.ui.common.LockedContent
import info.sergeikolinichenko.closednotepad.presentation.ui.home.daysLeftText
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.noteColorScheme
import info.sergeikolinichenko.closednotepad.presentation.security.LocalAuthenticator
import info.sergeikolinichenko.closednotepad.usecases.NoteText
import info.sergeikolinichenko.closednotepad.usecases.TrashPolicy

// Read-only view of a note in the trash
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashNoteScreen(viewModel: TrashViewModel, noteId: Long, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val note = state.notes.firstOrNull { it.id == noteId }
    val noteLock = LocalAppComponent.current.noteLock()
    val authenticator = LocalAuthenticator.current
    val unlocked by noteLock.unlocked.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    if (note == null) {
        if (state.loaded) LaunchedEffect(Unit) { onBack() }
        return
    }
    val colors = noteColorScheme(note.colorIndex)
    val hidden = note.isLocked && (note.id !in unlocked || note.isSealed)

    Scaffold(
        containerColor = colors.container,
        contentColor = colors.content,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.container,
                    navigationIconContentColor = colors.content,
                    actionIconContentColor = colors.content,
                    titleContentColor = colors.contentVariant
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                title = {
                    val now = System.currentTimeMillis()
                    Text(
                        daysLeftText(TrashPolicy.daysLeft(note.removedAt ?: now, now, state.daysBeforeDeletion)),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                actions = {
                    if (!hidden) {
                        IconButton(onClick = { viewModel.restore(setOf(note.id)); onBack() }) {
                            Icon(Icons.Outlined.RestoreFromTrash, contentDescription = stringResource(R.string.restore))
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = stringResource(R.string.delete_forever))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (hidden) {
            LockedContent(
                onUnlock = {
                    authenticator.authenticate(onSuccess = { noteLock.markUnlocked(listOf(note.id)) }, onError = {})
                },
                contentColor = colors.content,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                NoteText.displayTitle(note).ifEmpty { stringResource(R.string.untitled) },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (note.type == NoteType.TEXT) {
                Text(note.body, style = MaterialTheme.typography.bodyLarge)
            } else {
                note.items.filter { it.text.isNotBlank() }.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            item.text + if (item.quantity.isNotBlank()) " · ${item.quantity}" else "",
                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            text = pluralStringResource(R.plurals.confirm_delete_forever, 1, 1),
            confirm = stringResource(R.string.delete_forever),
            onConfirm = { confirmDelete = false; viewModel.deleteForever(setOf(note.id)); onBack() },
            onDismiss = { confirmDelete = false }
        )
    }
}
