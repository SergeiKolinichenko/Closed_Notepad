package info.sergeikolinichenko.closednotepad.presentation.ui.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RemoveDone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.LocalContentColor
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.presentation.navigation.LocalAppComponent
import info.sergeikolinichenko.closednotepad.presentation.security.Authenticator
import info.sergeikolinichenko.closednotepad.presentation.security.LocalAuthenticator
import info.sergeikolinichenko.closednotepad.presentation.ui.common.ColorPicker
import info.sergeikolinichenko.closednotepad.presentation.ui.common.LockedContent
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.noteColorScheme
import info.sergeikolinichenko.closednotepad.usecases.NoteText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    noteId: Long,
    onBack: () -> Unit
) {
    LaunchedEffect(noteId) { viewModel.start(noteId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        EditorState.Loading -> Unit
        EditorState.NotFound -> LaunchedEffect(Unit) { onBack() }
        is EditorState.Ready -> NoteEditor(viewModel, current.note, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditor(viewModel: EditorViewModel, note: Note, onBack: () -> Unit) {
    val context = LocalContext.current
    val authenticator = LocalAuthenticator.current
    val noteLock = LocalAppComponent.current.noteLock()
    val unlocked by noteLock.unlocked.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showColors by rememberSaveable { mutableStateOf(false) }
    var storeMode by rememberSaveable { mutableStateOf(false) }
    val colors = noteColorScheme(note.colorIndex)
    val hidden = note.isLocked && note.id !in unlocked

    fun showMessage(text: String) {
        scope.launch { snackbar.showSnackbar(text) }
    }

    fun unlock() {
        authenticator.authenticate(
            onSuccess = { noteLock.markUnlocked(listOf(note.id)) },
            onError = ::showMessage
        )
    }

    // Asks right away when a locked note is opened or the app comes back from the background
    LaunchedEffect(hidden) { if (hidden) unlock() }
    SecureWindow(enabled = note.isLocked)

    if (storeMode && !hidden) {
        StoreMode(viewModel = viewModel, note = note, onClose = { storeMode = false })
        return
    }

    Scaffold(
        containerColor = colors.container,
        contentColor = colors.content,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.container,
                    navigationIconContentColor = colors.content,
                    titleContentColor = colors.contentVariant,
                    actionIconContentColor = colors.content
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                title = {
                    if (!hidden) {
                        Text(
                            stringResource(if (saved) R.string.saved else R.string.saving),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                actions = {
                    if (!hidden) {
                        EditorActions(
                            note = note,
                            onPin = viewModel::togglePinned,
                            onLock = { lock ->
                                if (!lock) viewModel.setLocked(false)
                                else when (authenticator.availability()) {
                                    Authenticator.Availability.AVAILABLE -> viewModel.setLocked(true)
                                    else -> showMessage(context.getString(R.string.lock_needs_screen_lock))
                                }
                            },
                            onStoreMode = { storeMode = true },
                            onColor = { showColors = true },
                            onShare = { shareNote(context, note) },
                            onCopy = {
                                copyNote(context, note)
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    showMessage(context.getString(R.string.copied))
                                }
                            },
                            onConvert = viewModel::convertType,
                            onUncheckAll = viewModel::uncheckAll,
                            onRemoveChecked = viewModel::removeChecked,
                            onDelete = { viewModel.moveToTrash(onBack) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        CompositionLocalProvider(LocalContentColor provides colors.content) {
            Box(Modifier.padding(padding).fillMaxSize().imePadding()) {
                if (hidden) {
                    LockedContent(onUnlock = ::unlock, contentColor = colors.content)
                } else when (note.type) {
                    NoteType.TEXT -> TextEditor(note, colors, viewModel::setTitle, viewModel::setBody)
                    NoteType.CHECKLIST -> ChecklistEditor(note, colors, viewModel)
                    NoteType.SHOPPING -> ShoppingEditor(note, colors, viewModel)
                }
            }
        }
    }

    if (showColors) {
        ModalBottomSheet(onDismissRequest = { showColors = false }) {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text(stringResource(R.string.note_color), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                ColorPicker(selected = note.colorIndex, onSelect = viewModel::setColor)
            }
        }
    }
}

@Composable
private fun EditorActions(
    note: Note,
    onPin: () -> Unit,
    onLock: (Boolean) -> Unit,
    onStoreMode: () -> Unit,
    onColor: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onConvert: () -> Unit,
    onUncheckAll: () -> Unit,
    onRemoveChecked: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    if (note.type == NoteType.SHOPPING) {
        IconButton(onClick = onStoreMode) {
            Icon(Icons.Outlined.Storefront, contentDescription = stringResource(R.string.store_mode))
        }
    }
    // Toggles keep the note color; an unpinned pin is drawn lighter
    val content = LocalContentColor.current
    val toggleColors = IconButtonDefaults.iconToggleButtonColors(
        contentColor = content.copy(alpha = 0.55f),
        checkedContentColor = content
    )
    IconToggleButton(checked = note.isPinned, onCheckedChange = { onPin() }, colors = toggleColors) {
        Icon(
            Icons.Outlined.PushPin,
            contentDescription = stringResource(if (note.isPinned) R.string.unpin else R.string.pin)
        )
    }
    IconToggleButton(
        checked = note.isLocked,
        onCheckedChange = onLock,
        colors = IconButtonDefaults.iconToggleButtonColors(contentColor = content, checkedContentColor = content)
    ) {
        Icon(
            if (note.isLocked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
            contentDescription = stringResource(if (note.isLocked) R.string.unlock_note else R.string.lock_note)
        )
    }
    Box {
        IconButton(onClick = { menu = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more_options))
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.note_color)) },
                leadingIcon = { Icon(Icons.Outlined.Palette, null) },
                onClick = { menu = false; onColor() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share)) },
                leadingIcon = { Icon(Icons.Outlined.Share, null) },
                onClick = { menu = false; onShare() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.copy_text)) },
                leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) },
                onClick = { menu = false; onCopy() }
            )
            when (note.type) {
                NoteType.TEXT -> DropdownMenuItem(
                    text = { Text(stringResource(R.string.convert_to_list)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ListAlt, null) },
                    onClick = { menu = false; onConvert() }
                )
                NoteType.CHECKLIST, NoteType.SHOPPING -> {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.uncheck_all)) },
                        leadingIcon = { Icon(Icons.Outlined.RemoveDone, null) },
                        enabled = note.checkedCount > 0,
                        onClick = { menu = false; onUncheckAll() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.remove_checked)) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null) },
                        enabled = note.checkedCount > 0,
                        onClick = { menu = false; onRemoveChecked() }
                    )
                    if (note.type == NoteType.CHECKLIST) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.convert_to_text)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, null) },
                            onClick = { menu = false; onConvert() }
                        )
                    }
                }
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.move_to_trash)) },
                leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                onClick = { menu = false; onDelete() }
            )
        }
    }
}

// Keeps a locked note out of screenshots and the recent apps preview
@Composable
private fun SecureWindow(enabled: Boolean) {
    val activity = LocalActivity.current ?: return
    DisposableEffect(enabled) {
        if (enabled) activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (enabled) activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

private fun shareNote(context: Context, note: Note) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        if (note.title.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, note.title.trim())
        putExtra(Intent.EXTRA_TEXT, NoteText.toPlainText(note))
    }
    context.startActivity(Intent.createChooser(intent, null))
}

private fun copyNote(context: Context, note: Note) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    val clip = ClipData.newPlainText(NoteText.displayTitle(note), NoteText.toPlainText(note))
    if (note.isLocked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // Android 13+ does not show the content of sensitive clips in its copy preview
        clip.description.extras = PersistableBundle().apply {
            putBoolean(EXTRA_IS_SENSITIVE, true)
        }
    }
    clipboard.setPrimaryClip(clip)
}

// ClipDescription.EXTRA_IS_SENSITIVE, which older versions simply ignore
private const val EXTRA_IS_SENSITIVE = "android.content.extra.IS_SENSITIVE"
