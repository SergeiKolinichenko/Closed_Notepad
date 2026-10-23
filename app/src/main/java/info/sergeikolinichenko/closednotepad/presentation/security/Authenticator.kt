package info.sergeikolinichenko.closednotepad.presentation.security

import android.app.KeyguardManager
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import info.sergeikolinichenko.closednotepad.R

// Asks for the fingerprint, face or screen lock before a locked note is shown
class Authenticator(private val activity: FragmentActivity) {

    enum class Availability { AVAILABLE, NO_SCREEN_LOCK, UNAVAILABLE }

    fun availability(): Availability {
        val result = BiometricManager.from(activity).canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        return when {
            result == BiometricManager.BIOMETRIC_SUCCESS -> Availability.AVAILABLE
            !isDeviceSecure(activity) -> Availability.NO_SCREEN_LOCK
            else -> Availability.UNAVAILABLE
        }
    }

    // Without a screen lock the phone is open anyway, so locked notes open without a prompt;
    // otherwise they would stay inaccessible after the screen lock was turned off.
    fun authenticate(onSuccess: () -> Unit, onError: (String) -> Unit) {
        when (availability()) {
            Availability.NO_SCREEN_LOCK -> onSuccess()
            Availability.UNAVAILABLE -> onError(activity.getString(R.string.auth_unavailable))
            Availability.AVAILABLE -> showPrompt(onSuccess, onError)
        }
    }

    private fun showPrompt(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.auth_title))
            .setSubtitle(activity.getString(R.string.auth_subtitle))
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
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
