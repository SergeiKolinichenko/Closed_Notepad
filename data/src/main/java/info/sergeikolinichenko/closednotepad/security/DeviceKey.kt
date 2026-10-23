package info.sergeikolinichenko.closednotepad.security

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// A key in the phone's keystore that works only for a short time after the fingerprint
// or the screen lock was confirmed. It never leaves the secure hardware.
interface DeviceKey {

    // A key can be made only while the phone has a screen lock
    fun isAvailable(): Boolean

    fun exists(): Boolean

    fun create()

    // Null when the user hasn't just confirmed it's them, or the key is gone
    fun encrypt(plain: ByteArray): ByteArray?

    fun decrypt(box: ByteArray): ByteArray?
}

class KeystoreDeviceKey(private val context: Context) : DeviceKey {

    private val keyStore: KeyStore by lazy { KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) } }

    override fun isAvailable(): Boolean =
        context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    override fun exists(): Boolean = runCatching { keyStore.containsAlias(ALIAS) }.getOrDefault(false)

    override fun create() {
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(
                        VALIDITY_SECONDS,
                        KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setUserAuthenticationValidityDurationSeconds(VALIDITY_SECONDS)
                }
                // A new fingerprint doesn't make the key useless; removing the screen lock does
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) setInvalidatedByBiometricEnrollment(false)
            }
            .build()
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply { init(spec) }.generateKey()
    }

    override fun encrypt(plain: ByteArray): ByteArray? = withKey { key ->
        val cipher = Cipher.getInstance(AES_GCM).apply { init(Cipher.ENCRYPT_MODE, key) }
        cipher.iv + cipher.doFinal(plain)
    }

    override fun decrypt(box: ByteArray): ByteArray? = withKey { key ->
        val cipher = Cipher.getInstance(AES_GCM).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, box, 0, NONCE_BYTES))
        }
        cipher.doFinal(box, NONCE_BYTES, box.size - NONCE_BYTES)
    }

    private fun <T> withKey(block: (SecretKey) -> T): T? {
        return try {
            val key = keyStore.getKey(ALIAS, null) as? SecretKey ?: return null
            block(key)
        } catch (e: KeyPermanentlyInvalidatedException) {
            // The screen lock was removed or reset: this key will never work again
            runCatching { keyStore.deleteEntry(ALIAS) }
            null
        } catch (e: Exception) {
            // Mostly UserNotAuthenticatedException: the confirmation is too old or was too weak
            Log.w(TAG, "Keystore key can't be used: ${e.javaClass.simpleName}")
            null
        }
    }

    companion object {
        private const val TAG = "KeystoreDeviceKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "locked_notes_device_key"
        private const val AES_GCM = "AES/GCM/NoPadding"
        private const val NONCE_BYTES = 12
        private const val TAG_BITS = 128
        private const val VALIDITY_SECONDS = 30
    }
}
