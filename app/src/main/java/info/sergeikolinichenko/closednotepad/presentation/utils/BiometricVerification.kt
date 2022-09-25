package info.sergeikolinichenko.closednotepad.presentation.utils

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import info.sergeikolinichenko.closednotepad.R
import java.util.concurrent.Executor

class BiometricVerification(private val fragment: Fragment) {

    private val biometricManager = BiometricManager.from(fragment.requireActivity())
    private val executor = ContextCompat.getMainExecutor(fragment.requireActivity())

    fun readinessCheckBiometric(showMessage:(message: String)-> Unit):Boolean {
        var result = false
        when (biometricManager.canAuthenticate(DEVICE_CREDENTIAL or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> result = true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> showMessage(
                fragment.requireActivity().getString(R.string.hardware_not_available)
            )
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> showMessage(
                fragment.requireActivity().getString(R.string.hardware_unavailable_later)
            )
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                showMessage(fragment.requireActivity().getString(R.string.no_blocking_method))
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                showMessage(fragment.requireActivity().getString(R.string.biometrical_error_security))
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                showMessage(fragment.requireActivity().getString(R.string.boimetric_error_unsuported))
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                showMessage(fragment.requireActivity().getString(R.string.biometric_status_unknoun))
            }
        }
        return result
    }

    fun authUser(timeStamp: Long, act:(ts:Long)-> Unit, showMessage:(message: String)-> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(fragment.requireActivity().getString(R.string.authentication_required))
            .setSubtitle(fragment.requireActivity().getString(R.string.important_authentication))
            .setDescription(fragment.requireActivity().getString(R.string.please_authenticate))
            .setAllowedAuthenticators(DEVICE_CREDENTIAL or BIOMETRIC_WEAK)
            .build()
        val biomedicalPrompt = BiometricPrompt(fragment, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    act(timeStamp)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showMessage(
                        fragment.requireActivity().getString(R.string.authentication_error)
                                +"$errString"
                    )
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showMessage(
                        fragment.requireActivity().getString(R.string.authentication_failed)
                    )
                }
            })
        biomedicalPrompt.authenticate(promptInfo)
    }
}