package info.sergeikolinichenko.closednotepad.presentation.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.presentation.di.ApplicationComponent
import info.sergeikolinichenko.closednotepad.presentation.ui.editor.EditorScreen
import info.sergeikolinichenko.closednotepad.presentation.ui.editor.EditorViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.home.HomeScreen
import info.sergeikolinichenko.closednotepad.presentation.ui.home.HomeViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.search.SearchScreen
import info.sergeikolinichenko.closednotepad.presentation.ui.search.SearchViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.settings.SettingsScreen
import info.sergeikolinichenko.closednotepad.presentation.ui.settings.SettingsViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.trash.TrashNoteScreen
import info.sergeikolinichenko.closednotepad.presentation.ui.trash.TrashScreen
import info.sergeikolinichenko.closednotepad.presentation.ui.trash.TrashViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

val LocalAppComponent = staticCompositionLocalOf<ApplicationComponent> {
    error("ApplicationComponent is not provided")
}

private object Routes {
    const val HOME = "home"
    const val NOTE = "note/{id}"
    const val SEARCH = "search"
    const val TRASH = "trash"
    const val TRASH_NOTE = "trash/{id}"
    const val SETTINGS = "settings"
    const val ARG_ID = "id"

    fun note(id: Long) = "note/$id"
    fun trashNote(id: Long) = "trash/$id"
}

@Composable
fun AppNavHost(
    component: ApplicationComponent,
    openNoteRequests: Flow<Long>,
    navController: NavHostController = rememberNavController()
) {
    val factory = component.viewModelFactory()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Notes shared from other apps open right away
    LaunchedEffect(openNoteRequests) {
        openNoteRequests.collect { id -> navController.navigate(Routes.note(id)) { launchSingleTop = true } }
    }

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            component.uiMessages().messages.collect { message ->
                launch {
                    val text = message.count?.let { context.resources.getQuantityString(message.text, it, it) }
                        ?: context.getString(message.text)
                    val result = snackbarHostState.showSnackbar(
                        message = text,
                        actionLabel = message.undo?.let { context.getString(R.string.undo) },
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) message.undo?.invoke()
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel<HomeViewModel>(factory = factory),
                snackbarHostState = snackbarHostState,
                onOpenNote = { navController.navigate(Routes.note(it)) },
                onSearch = { navController.navigate(Routes.SEARCH) },
                onTrash = { navController.navigate(Routes.TRASH) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            Routes.NOTE,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.LongType })
        ) { entry ->
            EditorScreen(
                viewModel = viewModel<EditorViewModel>(factory = factory),
                noteId = entry.arguments?.getLong(Routes.ARG_ID) ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = viewModel<SearchViewModel>(factory = factory),
                onOpenNote = { navController.navigate(Routes.note(it)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.TRASH) {
            TrashScreen(
                viewModel = viewModel<TrashViewModel>(factory = factory),
                snackbarHostState = snackbarHostState,
                onOpenNote = { navController.navigate(Routes.trashNote(it)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Routes.TRASH_NOTE,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.LongType })
        ) { entry ->
            TrashNoteScreen(
                viewModel = viewModel<TrashViewModel>(factory = factory),
                noteId = entry.arguments?.getLong(Routes.ARG_ID) ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel<SettingsViewModel>(factory = factory),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
