package info.sergeikolinichenko.closednotepad.presentation.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import java.util.Calendar

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
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
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
                HomeTopBar(
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
        floatingActionButton = {
            if (!selecting) {
                FloatingActionButton(
                    onClick = { showCreateSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.create_note))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            FilterRow(state.filter, viewModel::setFilter)
            when {
                !state.loaded -> Unit
                state.notes.isEmpty() -> EmptyState(
                    hasAnyNotes = state.hasAnyNotes,
                    onCreate = { showCreateSheet = true }
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

    if (showCreateSheet) {
        CreateNoteSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { type ->
                showCreateSheet = false
                viewModel.createNote(type, onOpenNote)
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
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
    TopAppBar(
        title = { Text(stringResource(R.string.notes_title)) },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search))
            }
            if (layout == NotesLayout.GRID) {
                IconButton(onClick = { onLayout(NotesLayout.LIST) }) {
                    Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = stringResource(R.string.show_as_list))
                }
            } else {
                IconButton(onClick = { onLayout(NotesLayout.GRID) }) {
                    Icon(Icons.Outlined.GridView, contentDescription = stringResource(R.string.show_as_grid))
                }
            }
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more_options))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
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
    )
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
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(NoteFilter.entries) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(stringResource(filter.label)) }
            )
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
    val bottomPadding = 96.dp
    if (state.settings.layout == NotesLayout.GRID) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.notes, key = { it.id }) { note ->
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
    } else {
        val groups = groupNotes(state.notes, state.settings.sortOrder)
        LazyColumn(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            groups.forEach { (group, notes) ->
                if (group != null) {
                    item(key = "header-$group") {
                        Text(
                            stringResource(group.label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 2.dp).animateItem()
                        )
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

enum class NoteGroup(val label: Int) {
    PINNED(R.string.group_pinned),
    TODAY(R.string.group_today),
    YESTERDAY(R.string.group_yesterday),
    EARLIER(R.string.group_earlier),
    OTHERS(R.string.group_others)
}

// Feed groups: pinned first, then by the day of the last change when sorted by it
fun groupNotes(
    notes: List<Note>,
    order: SortOrder,
    now: Long = System.currentTimeMillis()
): List<Pair<NoteGroup?, List<Note>>> {
    val pinned = notes.filter { it.isPinned }
    val others = notes.filter { !it.isPinned }
    val result = mutableListOf<Pair<NoteGroup?, List<Note>>>()
    if (pinned.isNotEmpty()) result += NoteGroup.PINNED to pinned
    if (order != SortOrder.UPDATED_DESC) {
        if (others.isNotEmpty()) result += (if (pinned.isEmpty()) null else NoteGroup.OTHERS) to others
        return result
    }
    val startOfToday = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val startOfYesterday = Calendar.getInstance().apply {
        timeInMillis = startOfToday
        add(Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis
    others.groupBy { note ->
        when {
            note.updatedAt >= startOfToday -> NoteGroup.TODAY
            note.updatedAt >= startOfYesterday -> NoteGroup.YESTERDAY
            else -> NoteGroup.EARLIER
        }
    }.toSortedMap().forEach { (group, list) -> result += group to list }
    return result
}

@Composable
private fun EmptyState(hasAnyNotes: Boolean, onCreate: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.EditNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(if (hasAnyNotes) R.string.empty_filter_title else R.string.empty_title),
            style = MaterialTheme.typography.titleMedium,
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
            androidx.compose.material3.FilledTonalButton(onClick = onCreate) {
                Text(stringResource(R.string.create_note))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNoteSheet(onDismiss: () -> Unit, onCreate: (NoteType) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.create),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            CreateTile(NoteType.TEXT, R.string.type_text_hint, colorIndex = 1, onCreate)
            CreateTile(NoteType.CHECKLIST, R.string.type_checklist_hint, colorIndex = 2, onCreate)
            CreateTile(NoteType.SHOPPING, R.string.type_shopping_hint, colorIndex = 4, onCreate)
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                stringResource(R.string.more_types_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
