package info.sergeikolinichenko.closednotepad.presentation.security

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.security.NotesVault

// The password dialogs of locked notes: setting it up, opening notes with it, changing it
@Composable
fun VaultDialogs(authenticator: Authenticator) {
    val request by authenticator.request.collectAsStateWithLifecycle()
    val busy by authenticator.busy.collectAsStateWithLifecycle()
    when (request) {
        null -> Unit
        is Authenticator.Request.Password -> UnlockDialog(authenticator, busy)
        is Authenticator.Request.SetUp -> NewPasswordDialog(
            title = R.string.vault_setup_title,
            intro = R.string.vault_setup_body,
            confirmLabel = R.string.vault_encrypt,
            busy = busy,
            onDismiss = authenticator::dismiss,
            onConfirm = authenticator::submitSetUp
        )
        Authenticator.Request.ChangePassword -> {
            val context = LocalContext.current
            NewPasswordDialog(
                title = R.string.vault_change_password,
                intro = null,
                confirmLabel = R.string.save,
                busy = busy,
                onDismiss = authenticator::dismiss,
                onConfirm = { password ->
                    authenticator.submitNewPassword(password) {
                        Toast.makeText(context, R.string.vault_password_changed, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun UnlockDialog(authenticator: Authenticator, busy: Boolean) {
    var password by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = authenticator::dismiss,
        title = { Text(stringResource(R.string.vault_unlock_title)) },
        text = {
            Column {
                Text(stringResource(R.string.vault_unlock_body))
                Spacer(Modifier.height(16.dp))
                PasswordField(
                    value = password,
                    onValueChange = { password = it; wrong = false },
                    label = R.string.vault_password,
                    error = if (wrong) R.string.vault_wrong_password else null,
                    enabled = !busy
                )
                if (busy) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && password.isNotEmpty(),
                onClick = { authenticator.submitPassword(password.toCharArray()) { wrong = true } }
            ) { Text(stringResource(R.string.unlock)) }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = authenticator::dismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun NewPasswordDialog(
    title: Int,
    intro: Int?,
    confirmLabel: Int,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (CharArray) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    var understood by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    val tooShort = password.length < NotesVault.MIN_PASSWORD_LENGTH
    val mismatch = password != repeat

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (intro != null) {
                    Text(stringResource(intro))
                    Spacer(Modifier.height(12.dp))
                }
                VaultWarnings()
                Spacer(Modifier.height(16.dp))
                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = R.string.vault_password,
                    error = if (showErrors && tooShort) R.string.vault_password_short else null,
                    enabled = !busy
                )
                Spacer(Modifier.height(8.dp))
                PasswordField(
                    value = repeat,
                    onValueChange = { repeat = it },
                    label = R.string.vault_password_repeat,
                    error = if (showErrors && !tooShort && mismatch) R.string.vault_password_mismatch else null,
                    enabled = !busy
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(value = understood, enabled = !busy, role = Role.Checkbox, onValueChange = { understood = it })
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = understood, onCheckedChange = null)
                    Text(stringResource(R.string.vault_understand), modifier = Modifier.padding(start = 12.dp))
                }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && understood,
                onClick = {
                    showErrors = true
                    if (!tooShort && !mismatch) onConfirm(password.toCharArray())
                }
            ) { Text(stringResource(confirmLabel)) }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

// What the user must know about locked notes: the title stays readable, and a lost password is final
@Composable
fun VaultWarnings() {
    Text(
        stringResource(R.string.vault_warning_titles),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.vault_warning_no_recovery),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, label: Int, error: Int?, enabled: Boolean) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = stringResource(if (visible) R.string.vault_hide_password else R.string.vault_show_password)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
