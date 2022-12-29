package info.sergeikolinichenko.closednotepad.presentation.utils

import android.view.View
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import info.sergeikolinichenko.closednotepad.R
import java.util.concurrent.Executor

/** Created by Sergei Kolinichenko on 08.12.2022 at 21:08 (GMT+3) **/

fun readinessCheckBiometric(
    fragment: Fragment,
    biometricManager: BiometricManager,
    objectAnchor: View,
    isNight: Boolean,
    showMessage:(viewContainer: View, anchorObject: View, isNight: Boolean, message: String)-> Unit
):Boolean {
    var result = false

    val container = fragment.requireActivity()
        .findViewById<FragmentContainerView>(R.id.main_container)

    when (biometricManager.canAuthenticate(DEVICE_CREDENTIAL or BIOMETRIC_WEAK)
    ) {
        BiometricManager.BIOMETRIC_SUCCESS -> result = true
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            showMessage(
                container,
                objectAnchor,
                isNight,
            fragment.requireActivity().getString(R.string.hardware_not_available)
        )
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> showMessage(
            container,
            objectAnchor,
            isNight,
            fragment.requireActivity().getString(R.string.hardware_unavailable_later)
        )
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            showMessage(
                container,
                objectAnchor,
                isNight,
                fragment.requireActivity().getString(R.string.no_blocking_method))
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
            showMessage(
                container,
                objectAnchor,
                isNight,
                fragment.requireActivity().getString(R.string.biometrical_error_security))
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            showMessage(
                container,
                objectAnchor,
                isNight,
                fragment.requireActivity().getString(R.string.boimetric_error_unsuported))
        }
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            showMessage(
                container,
                objectAnchor,
                isNight,
                fragment.requireActivity().getString(R.string.biometric_status_unknoun))
        }
    }
    return result
}

fun authUser(
    fragment: Fragment,
    executor: Executor,
    objectAnchor: View,
    isNight: Boolean,
    timeStamp: Long,
    act:(ts:Long)-> Unit,
    showMessage:(viewContainer: View, anchorObject: View, isNight: Boolean, message: String)-> Unit
) {

    val container = fragment.requireActivity()
        .findViewById<FragmentContainerView>(R.id.main_container)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(fragment.requireActivity().getString(R.string.authentication_required))
        .setSubtitle(fragment.requireActivity().getString(R.string.important_authentication))
        .setDescription(fragment.requireActivity().getString(R.string.please_authenticate))
        .setAllowedAuthenticators( DEVICE_CREDENTIAL or BIOMETRIC_WEAK )
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
                    container,
                    objectAnchor,
                    isNight,
                    fragment.requireActivity().getString(
                        R.string.authentication_error,
                        errString,
                        errorCode.toString()
                    )
                )
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                showMessage(
                    container,
                    objectAnchor,
                    isNight,
                    fragment.requireActivity().getString(R.string.authentication_failed)
                )
            }
        })
    biomedicalPrompt.authenticate(promptInfo)
}