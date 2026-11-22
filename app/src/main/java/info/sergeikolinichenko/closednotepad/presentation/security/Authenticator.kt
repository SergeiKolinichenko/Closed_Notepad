package info.sergeikolinichenko.closednotepad.presentation.security

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.security.NotesVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Asks for the fingerprint, face or screen lock before a locked note is shown, and opens the
// vault of encrypted notes with it. When the phone can't open the vault (no screen lock, the
// keystore key is gone, the sensor is locked out) it asks for the password instead.
class Authenticator(private val activity: FragmentActivity, private val vault: NotesVault) {

    enum class Availability { AVAILABLE, NO_SCREEN_LOCK, UNAVAILABLE }

    // Dialogs drawn by VaultDialogs
    sealed interface Request {
        data class Password(val onSuccess: () -> Unit) : Request
        data class SetUp(val onDone: () -> Unit) : Request
        data object ChangePassword : Request
    }

    private val _request = MutableStateFlow<Request?>(null)
    val request: StateFlow<Request?> = _request.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    // The keystore key accepts only strong biometrics or the screen lock. Android 9 and 10
    // can't ask for exactly that, so they ask for any; a weak face then leads to the password.
    private val authenticators =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        else BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    fun availability(): Availability {
        val result = BiometricManager.from(activity).canAuthenticate(authenticators)
        return when {
            result == BiometricManager.BIOMETRIC_SUCCESS -> Availability.AVAILABLE
            !isDeviceSecure(activity) -> Availability.NO_SCREEN_LOCK
            else -> Availability.UNAVAILABLE
        }
    }

    fun authenticate(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!vault.isSetUp.value) {
            confirm(onSuccess, onError)
            return
        }
        when (availability()) {
            Availability.AVAILABLE -> showPrompt(
                subtitle = R.string.auth_subtitle,
                onSuccess = { openVault(onSuccess) },
                // Locked out after too many tries, or the sensor failed: the password still works
                onError = { askPassword(onSuccess) }
            )
            Availability.NO_SCREEN_LOCK, Availability.UNAVAILABLE -> askPassword(onSuccess)
        }
    }

    // Sets the password and encrypts locked notes. The user confirms it's them first,
    // otherwise anyone holding the phone could set a password and read the notes with it.
    fun setUp(onDone: () -> Unit, onError: (String) -> Unit) {
        if (vault.isSetUp.value) {
            onDone()
            return
        }
        confirm(onSuccess = { _request.value = Request.SetUp(onDone) }, onError = onError)
    }

    fun changePassword(onError: (String) -> Unit) {
        authenticate(onSuccess = { _request.value = Request.ChangePassword }, onError = onError)
    }

    fun dismiss() {
        if (!_busy.value) _request.value = null
    }

    // Answers of the dialogs. Password hashing and a new key take a second or two.

    fun submitPassword(password: CharArray, onWrong: () -> Unit) {
        val request = _request.value as? Request.Password ?: return
        work({ vault.unlockWithPassword(password) }) { ok ->
            if (!ok) return@work onWrong()
            _request.value = null
            request.onSuccess()
            offerDeviceUnlock()
        }
    }

    fun submitSetUp(password: CharArray) {
        val request = _request.value as? Request.SetUp ?: return
        work({ vault.setUp(password) }) {
            _request.value = null
            offerDeviceUnlock()
            request.onDone()
        }
    }

    fun submitNewPassword(password: CharArray, onSaved: () -> Unit) {
        work({ vault.changePassword(password) }) { ok ->
            _request.value = null
            if (ok) onSaved()
        }
    }

    private fun <T> work(block: () -> T, then: (T) -> Unit) {
        _busy.value = true
        activity.lifecycleScope.launch {
            val result = try {
                withContext(Dispatchers.Default) { block() }
            } finally {
                _busy.value = false
            }
            then(result)
        }
    }

    private fun openVault(onSuccess: () -> Unit) {
        if (vault.unlockWithDevice()) onSuccess() else askPassword(onSuccess)
    }

    private fun askPassword(onSuccess: () -> Unit) {
        _request.value = Request.Password(onSuccess)
    }

    // After the password: let the fingerprint or screen lock open the notes again
    private fun offerDeviceUnlock() {
        if (!vault.needsDeviceUnlock() || availability() != Availability.AVAILABLE) return
        vault.prepareDeviceKey()
        // A confirmation from a few seconds ago still counts
        if (vault.enableDeviceUnlock()) return
        showPrompt(subtitle = R.string.vault_enable_device, onSuccess = { vault.enableDeviceUnlock() }, onError = {})
    }

    // Without a screen lock the phone is open anyway, so notes that are not encrypted yet open
    // without a prompt; otherwise they would stay inaccessible after the screen lock was turned off.
    private fun confirm(onSuccess: () -> Unit, onError: (String) -> Unit) {
        when (availability()) {
            Availability.NO_SCREEN_LOCK -> onSuccess()
            Availability.UNAVAILABLE -> onError(activity.getString(R.string.auth_unavailable))
            Availability.AVAILABLE -> showPrompt(R.string.auth_subtitle, onSuccess, onError)
        }
    }

    private fun showPrompt(subtitle: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.auth_title))
            .setSubtitle(activity.getString(subtitle))
            .setAllowedAuthenticators(authenticators)
            .build()
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    if (!cancelled) onError(errString.toString())
                }
            }
        )
        prompt.authenticate(promptInfo)
    }

    companion object {
        fun isDeviceSecure(context: Context): Boolean =
            context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true
    }
}

val LocalAuthenticator = staticCompositionLocalOf<Authenticator> {
    error("Authenticator is not provided")
}
