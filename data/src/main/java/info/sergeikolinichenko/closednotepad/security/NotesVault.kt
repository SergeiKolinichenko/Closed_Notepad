package info.sergeikolinichenko.closednotepad.security

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.PrivateKey
import java.security.PublicKey

// Keeps the key pair of locked notes. The public key seals notes and is always at hand.
// The private key opens them; it is stored twice, wrapped by the keystore key (opened with
// the fingerprint or screen lock) and by the user's password (the spare way in), and it is
// kept in memory only while the app is in front.
class NotesVault(
    private val prefs: SharedPreferences,
    private val deviceKey: DeviceKey,
    private val passwordIterations: Int = PASSWORD_ITERATIONS
) {

    private val _isSetUp = MutableStateFlow(readIsSetUp())
    val isSetUp: StateFlow<Boolean> = _isSetUp.asStateFlow()

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    @Volatile
    private var privateKey: PrivateKey? = null

    @Volatile
    private var publicKey: PublicKey? = null

    // Opened notes, so lists don't decrypt every note on every change
    private val opened = object : LinkedHashMap<String, String>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?) = size > CACHE_SIZE
    }

    // Slow (a new RSA key and the password hashing), call it off the main thread
    @Synchronized
    fun setUp(password: CharArray) {
        check(!readIsSetUp()) { "The vault is already set up" }
        val pair = NoteCipher.generateKeyPair()
        val saved = prefs.edit()
            .putInt(KEY_VERSION, VERSION)
            .putString(KEY_PUBLIC, NoteCipher.encode(pair.public.encoded))
            .putString(KEY_BY_PASSWORD, NoteCipher.wrapWithPassword(pair.private.encoded, password, passwordIterations))
            .remove(KEY_BY_DEVICE)
            .commit()
        check(saved) { "Can't save the vault" }
        publicKey = pair.public
        privateKey = pair.private
        _isSetUp.value = true
        _unlocked.value = true
    }

    // Right after the fingerprint or screen lock was confirmed. False means the password is needed.
    @Synchronized
    fun unlockWithDevice(): Boolean {
        if (privateKey != null) return true
        val wrapped = prefs.getString(KEY_BY_DEVICE, null) ?: return false
        val encoded = deviceKey.decrypt(NoteCipher.decode(wrapped))
        if (encoded == null) {
            // The keystore key is gone for good: the screen lock was removed, or the app was moved to a new phone
            if (!deviceKey.exists()) prefs.edit().remove(KEY_BY_DEVICE).commit()
            return false
        }
        return openWith(encoded)
    }

    // Slow (the password hashing), call it off the main thread. False means a wrong password.
    fun unlockWithPassword(password: CharArray): Boolean {
        val wrapped = prefs.getString(KEY_BY_PASSWORD, null) ?: return false
        val encoded = NoteCipher.unwrapWithPassword(wrapped, password) ?: return false
        return synchronized(this) { privateKey != null || openWith(encoded) }
    }

    private fun openWith(encoded: ByteArray): Boolean {
        val key = runCatching { NoteCipher.privateKey(encoded) }.getOrNull()
        encoded.fill(0)
        if (key == null) return false
        privateKey = key
        _unlocked.value = true
        return true
    }

    // After a password unlock: the fingerprint can open the notes again once it is confirmed
    fun needsDeviceUnlock(): Boolean =
        _isSetUp.value && deviceKey.isAvailable() && !prefs.contains(KEY_BY_DEVICE)

    // Made before the confirmation, so the confirmation covers it
    fun prepareDeviceKey() {
        if (deviceKey.isAvailable() && !deviceKey.exists()) {
            runCatching { deviceKey.create() }.onFailure { Log.w(TAG, "Can't create the keystore key", it) }
        }
    }

    // Right after the fingerprint or screen lock was confirmed, while the vault is unlocked
    @Synchronized
    fun enableDeviceUnlock(): Boolean {
        val key = privateKey ?: return false
        prepareDeviceKey()
        val wrapped = deviceKey.encrypt(key.encoded) ?: return false
        return prefs.edit().putString(KEY_BY_DEVICE, NoteCipher.encode(wrapped)).commit()
    }

    // Slow (the password hashing), call it off the main thread
    fun changePassword(newPassword: CharArray): Boolean {
        val key = privateKey ?: return false
        val wrapped = NoteCipher.wrapWithPassword(key.encoded, newPassword, passwordIterations)
        return prefs.edit().putString(KEY_BY_PASSWORD, wrapped).commit()
    }

    fun lock() {
        synchronized(this) { privateKey = null }
        synchronized(opened) { opened.clear() }
        _unlocked.value = false
    }

    fun seal(plain: String): String = NoteCipher.seal(plain, requirePublicKey())

    // Null while the vault is locked, or if the note can't be opened
    fun open(sealed: String): String? {
        val key = privateKey ?: return null
        synchronized(opened) { opened[sealed] }?.let { return it }
        val plain = runCatching { NoteCipher.open(sealed, key) }
            .onFailure { Log.e(TAG, "Can't open a locked note", it) }
            .getOrNull() ?: return null
        synchronized(opened) { opened[sealed] = plain }
        return plain
    }

    private fun requirePublicKey(): PublicKey {
        publicKey?.let { return it }
        val encoded = prefs.getString(KEY_PUBLIC, null) ?: error("The vault is not set up")
        return NoteCipher.publicKey(NoteCipher.decode(encoded)).also { publicKey = it }
    }

    private fun readIsSetUp(): Boolean = prefs.contains(KEY_PUBLIC) && prefs.contains(KEY_BY_PASSWORD)

    companion object {
        // Stored apart from the settings; it is in the backup too, so the password opens notes on a new phone
        const val PREFS_NAME = "locked_notes_vault"
        const val MIN_PASSWORD_LENGTH = 6

        // OWASP's figure for PBKDF2-HMAC-SHA256
        const val PASSWORD_ITERATIONS = 600_000

        private const val TAG = "NotesVault"
        private const val VERSION = 1
        private const val CACHE_SIZE = 64
        private const val KEY_VERSION = "version"
        private const val KEY_PUBLIC = "public_key"
        private const val KEY_BY_PASSWORD = "private_key_by_password"
        private const val KEY_BY_DEVICE = "private_key_by_device"
    }
}
