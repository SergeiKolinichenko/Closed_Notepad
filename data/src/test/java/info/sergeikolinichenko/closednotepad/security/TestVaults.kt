package info.sergeikolinichenko.closednotepad.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider

// A keystore stand-in: confirming it's the user is a flag the test sets
class FakeDeviceKey : DeviceKey {

    var available = true
    var confirmed = false
    private var key: ByteArray? = null

    override fun isAvailable() = available

    override fun exists() = key != null

    override fun create() {
        key = ByteArray(32) { it.toByte() }
    }

    override fun encrypt(plain: ByteArray): ByteArray? =
        key?.takeIf { confirmed }?.let { NoteCipher.aesEncrypt(it, plain, AAD) }

    override fun decrypt(box: ByteArray): ByteArray? =
        key?.takeIf { confirmed }?.let { NoteCipher.aesDecrypt(it, box, AAD) }

    // What happens when the screen lock is removed
    fun lose() {
        key = null
    }

    private companion object {
        val AAD = "fake".toByteArray()
    }
}

object TestVaults {

    // Few iterations, so the tests stay fast
    const val ITERATIONS = 1_000

    fun vault(deviceKey: DeviceKey = FakeDeviceKey(), name: String = "test_vault"): NotesVault {
        val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences(name, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        return NotesVault(prefs, deviceKey, ITERATIONS)
    }
}
