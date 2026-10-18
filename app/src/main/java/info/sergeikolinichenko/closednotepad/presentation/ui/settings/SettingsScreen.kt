package info.sergeikolinichenko.closednotepad.presentation.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Settings
import info.sergeikolinichenko.closednotepad.presentation.ui.common.ColorPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                title = { Text(stringResource(R.string.settings)) }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.default_note_color), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            ColorPicker(selected = settings.defaultColorIndex, onSelect = viewModel::setDefaultColor)

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Text(stringResource(R.string.trash_auto_delete), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.trash_auto_delete_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Settings.DAYS_BEFORE_DELETION_OPTIONS.forEach { days ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = settings.daysBeforeDeletion == days,
                            role = Role.RadioButton,
                            onClick = { viewModel.setDaysBeforeDeletion(days) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = settings.daysBeforeDeletion == days, onClick = null)
                    Text(
                        if (days == 0) stringResource(R.string.never_delete)
                        else pluralStringResource(R.plurals.after_days, days, days),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Text(stringResource(R.string.about_locked_notes), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.about_locked_notes_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (version != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.version, version),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
