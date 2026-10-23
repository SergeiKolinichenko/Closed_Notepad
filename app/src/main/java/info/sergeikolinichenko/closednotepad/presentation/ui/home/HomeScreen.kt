package info.sergeikolinichenko.closednotepad.presentation.ui.home

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.models.NotesLayout
import info.sergeikolinichenko.closednotepad.models.SortOrder
import info.sergeikolinichenko.closednotepad.presentation.navigation.LocalAppComponent
import info.sergeikolinichenko.closednotepad.presentation.security.LocalAuthenticator
import info.sergeikolinichenko.closednotepad.presentation.ui.common.ColorPicker
import info.sergeikolinichenko.closednotepad.presentation.ui.common.Seal
import info.sergeikolinichenko.closednotepad.presentation.ui.common.icon
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.GroupHeaderStyle
import info.sergeikolinichenko.closednotepad.usecases.NoteGroup
import info.sergeikolinichenko.closednotepad.usecases.NoteGrouper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenNote: (Long) -> Unit,
    onSearch: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val authenticator = LocalAuthenticator.current
    val noteLock = LocalAppComponent.current.noteLock()
    val unlocked by noteLock.unlocked.collectAsStateWithLifecycle()
    val offerEncryption by viewModel.offerEncryption.collectAsStateWithLifecycle()
    var showColorSheet by rememberSaveable { mutableStateOf(false) }
    val selecting = state.selected.isNotEmpty()

    BackHandler(enabled = selecting) { viewModel.clearSelection() }

    // Changing locked notes needs the same check as opening them
    fun withUnlocked(notes: List<Note>, action: () -> Unit) {
        val locked = notes.filter { it.isLocked && it.id !in unlocked }
        if (locked.isEmpty()) action()
        else authenticator.authenticate(
            onSuccess = { noteLock.markUnlocked(locked.map { it.id }); action() },
            onError = {}
        )
    }

    Scaffold(
        topBar = {
            if (selecting) {
                SelectionTopBar(
                    count = state.selected.size,
                    onClose = viewModel::clearSelection,
                    onPin = viewModel::pinSelected,
                    onColor = { showColorSheet = true },
                    onDelete = { withUnlocked(state.selectedNotes) { viewModel.trashSelected() } }
                )
            } else {
                HomeHeader(
                    layout = state.settings.layout,
                    sortOrder = state.settings.sortOrder,
                    onSearch = onSearch,
                    onLayout = viewModel::setLayout,
                    onSort = viewModel::setSortOrder,
                    onTrash = onTrash,
                    onSettings = onSettings
                )
            }
        },
        bottomBar = {
            if (!selecting) {
                QuickBar(
                    onSave = viewModel::saveQuickNote,
                    onCreate = { type -> viewModel.createNote(type, onOpenNote) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (offerEncryption && !selecting) {
                EncryptionBanner(onSetUp = { authenticator.setUp(onDone = {}, onError = {}) })
            }
            FilterRow(state.filter, viewModel::setFilter)
            when {
                !state.loaded -> Unit
                state.notes.isEmpty() -> EmptyState(
                    hasAnyNotes = state.hasAnyNotes,
                    onCreate = { viewModel.createNote(NoteType.TEXT, onOpenNote) }
                )
                else -> NotesContent(
                    state = state,
                    onClick = { note ->
                        if (selecting) viewModel.toggleSelection(note.id) else onOpenNote(note.id)
                    },
                    onLongClick = { note -> viewModel.toggleSelection(note.id) }
                )
            }
        }
    }

    if (showColorSheet) {
        ModalBottomSheet(onDismissRequest = { showColorSheet = false }) {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text(stringResource(R.string.note_color), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                ColorPicker(
                    selected = state.selectedNotes.map { it.colorIndex }.distinct().singleOrNull() ?: -1,
                    onSelect = {
                        showColorSheet = false
                        viewModel.setColorOfSelected(it)
                    }
                )
            }
        }
    }
}

// A large title with today's date instead of an app bar: the screen is a sheet of paper
@Composable
private fun HomeHeader(
    layout: NotesLayout,
    sortOrder: SortOrder,
    onSearch: () -> Unit,
    onLayout: (NotesLayout) -> Unit,
    onSort: (SortOrder) -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    val today = remember { todayText() }
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 8.dp, top = 20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(Modifier.weight(1f)) {
            Text(today, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.notes_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() }
            )
        }
        IconButton(onClick = onSearch) {
            Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search))
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.more_options))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                if (layout == NotesLayout.GRID) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_as_list)) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ViewList, null) },
                        onClick = { menu = false; onLayout(NotesLayout.LIST) }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_as_grid)) },
                        leadingIcon = { Icon(Icons.Outlined.GridView, null) },
                        onClick = { menu = false; onLayout(NotesLayout.GRID) }
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.sort)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Sort, null) },
                    onClick = { menu = false; sortMenu = true }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.trash)) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                    onClick = { menu = false; onTrash() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings)) },
                    leadingIcon = { Icon(Icons.Outlined.Settings, null) },
                    onClick = { menu = false; onSettings() }
                )
            }
            DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = { Text(stringResource(order.label)) },
                        trailingIcon = { if (order == sortOrder) Icon(Icons.Outlined.Check, null) },
                        onClick = { sortMenu = false; onSort(order) }
                    )
                }
            }
        }
    }
}

// "Monday, 19 October" in the language of the phone
private fun todayText(): String {
    val locale = Locale.getDefault()
    // ICU may answer with the stand-alone weekday "cccc", which older java.text.SimpleDateFormat rejects
    val pattern = DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM").replace('c', 'E')
    return SimpleDateFormat(pattern, locale).format(Date())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

// The main action is always at hand: type a line and save it, or start a list
@Composable
private fun QuickBar(onSave: (String) -> Unit, onCreate: (NoteType) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface)
    fun save() {
        onSave(text)
        text = ""
    }
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surfaceContainerHigh)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(28.dp))
                .padding(start = 20.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = style,
                cursorBrush = SolidColor(colors.tertiary),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text(stringResource(R.string.write_hint), style = style, color = colors.onSurfaceVariant)
                    }
                    inner()
                }
            )
            if (text.isEmpty()) {
                IconButton(onClick = { onCreate(NoteType.CHECKLIST) }) {
                    Icon(NoteType.CHECKLIST.icon, contentDescription = stringResource(R.string.new_checklist), tint = colors.onSurfaceVariant)
                }
                IconButton(onClick = { onCreate(NoteType.SHOPPING) }) {
                    Icon(NoteType.SHOPPING.icon, contentDescription = stringResource(R.string.new_shopping), tint = colors.onSurfaceVariant)
                }
            }
        }
        FilledIconButton(
            onClick = { if (text.isBlank()) onCreate(NoteType.TEXT) else save() },
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            modifier = Modifier.size(56.dp)
        ) {
            if (text.isBlank()) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.create_note))
            } else {
                Icon(Icons.Outlined.Check, contentDescription = stringResource(R.string.save_note))
            }
        }
    }
}

val SortOrder.label: Int
    get() = when (this) {
        SortOrder.UPDATED_DESC -> R.string.sort_updated
        SortOrder.CREATED_DESC -> R.string.sort_created_desc
        SortOrder.CREATED_ASC -> R.string.sort_created_asc
        SortOrder.TITLE_ASC -> R.string.sort_title_asc
        SortOrder.TITLE_DESC -> R.string.sort_title_desc
        SortOrder.COLOR -> R.string.sort_color
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onPin: () -> Unit,
    onColor: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cancel_selection))
            }
        },
        title = { Text(count.toString()) },
        actions = {
            IconButton(onClick = onPin) {
                Icon(Icons.Outlined.PushPin, contentDescription = stringResource(R.string.pin))
            }
            IconButton(onClick = onColor) {
                Icon(Icons.Outlined.Palette, contentDescription = stringResource(R.string.note_color))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.move_to_trash))
            }
        }
    )
}

@Composable
private fun FilterRow(selected: NoteFilter, onSelect: (NoteFilter) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)
            .selectableGroup()
    ) {
        NoteFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Column(
                Modifier
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(filter) })
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(filter.label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent)
                )
            }
        }
    }
}

private val NoteFilter.label: Int
    get() = when (this) {
        NoteFilter.ALL -> R.string.filter_all
        NoteFilter.TEXT -> R.string.filter_text
        NoteFilter.CHECKLIST -> R.string.filter_checklist
        NoteFilter.SHOPPING -> R.string.filter_shopping
    }

@Composable
private fun NotesContent(
    state: HomeState,
    onClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit
) {
    val bottomPadding = 16.dp
    val groups = remember(state.notes, state.settings.sortOrder) {
        NoteGrouper.group(state.notes, state.settings.sortOrder)
    }
    if (state.settings.layout == NotesLayout.GRID) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            groups.forEach { (group, notes) ->
                if (group != null) {
                    item(key = "header-$group", span = StaggeredGridItemSpan.FullLine) {
                        GroupHeader(group, Modifier.animateItem())
                    }
                }
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        selected = note.id in state.selected,
                        compact = false,
                        onClick = { onClick(note) },
                        onLongClick = { onLongClick(note) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            groups.forEach { (group, notes) ->
                if (group != null) {
                    item(key = "header-$group") {
                        GroupHeader(group, Modifier.animateItem())
                    }
                }
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        selected = note.id in state.selected,
                        compact = true,
                        onClick = { onClick(note) },
                        onLongClick = { onLongClick(note) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(group: NoteGroup, modifier: Modifier = Modifier) {
    Text(
        group.label().uppercase(),
        style = GroupHeaderStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(start = 6.dp, top = 14.dp, bottom = 2.dp)
            .semantics { heading() }
    )
}

@Composable
private fun NoteGroup.label(): String = when (this) {
    NoteGroup.Pinned -> stringResource(R.string.group_pinned)
    NoteGroup.Others -> stringResource(R.string.group_others)
    NoteGroup.Today -> stringResource(R.string.group_today)
    NoteGroup.Yesterday -> stringResource(R.string.group_yesterday)
    NoteGroup.ThisWeek -> stringResource(R.string.group_this_week)
    NoteGroup.ThisMonth -> stringResource(R.string.group_this_month)
    is NoteGroup.Month -> {
        val locale = Locale.getDefault()
        remember(this, locale) { monthText(year, month, locale) }
    }
}

// "September" for this year, "September 2025" for earlier ones, in the language of the phone
private fun monthText(year: Int, month: Int, locale: Locale): String {
    val calendar = Calendar.getInstance()
    val pattern = if (year == calendar.get(Calendar.YEAR)) "LLLL" else "LLLL yyyy"
    calendar.clear()
    calendar.set(year, month, 1)
    return SimpleDateFormat(pattern, locale).format(calendar.time)
}

// Shown after the update until the password is set and the locked notes are encrypted
@Composable
private fun EncryptionBanner(onSetUp: () -> Unit) {
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Seal(size = 32.dp)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                stringResource(R.string.vault_banner_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                stringResource(R.string.vault_banner_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        TextButton(onClick = onSetUp) { Text(stringResource(R.string.vault_set_password)) }
    }
}

@Composable
private fun EmptyState(hasAnyNotes: Boolean, onCreate: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(if (hasAnyNotes) R.string.empty_filter_title else R.string.empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        if (!hasAnyNotes) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onCreate) {
                Text(stringResource(R.string.create_note))
            }
        }
    }
}
