package info.sergeikolinichenko.closednotepad.security

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

// The crypto behind locked notes. Every note is sealed with its own AES-256-GCM key, and that
// key is wrapped with the vault's RSA public key. So a locked note can be saved at any time,
// even after the app went to the background, while opening it needs the private key.
// The private key itself is kept wrapped by a password (and by the phone's keystore, see NotesVault).
object NoteCipher {

    const val PREFIX = "sealed:v1:"

    private const val RSA_BITS = 3072
    private const val AES_KEY_BYTES = 32
    private const val NONCE_BYTES = 12
    private const val TAG_BITS = 128
    private const val SALT_BYTES = 16
    private const val RSA_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private const val AES_GCM = "AES/GCM/NoPadding"
    private const val HMAC = "HmacSHA256"

    private val NOTE_AAD = "closednotepad/note/v1".toByteArray()
    private val PASSWORD_AAD = "closednotepad/password/v1".toByteArray()
    private val oaep = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)
    private val random = SecureRandom()

    fun isSealed(body: String): Boolean = body.startsWith(PREFIX)

    fun generateKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply { initialize(RSA_BITS, random) }.generateKeyPair()

    fun publicKey(encoded: ByteArray): PublicKey =
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(encoded))

    fun privateKey(encoded: ByteArray): PrivateKey =
        KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(encoded))

    // PREFIX + base64(length of the wrapped key | wrapped key | nonce | ciphertext with tag)
    fun seal(plain: String, publicKey: PublicKey): String {
        val noteKey = ByteArray(AES_KEY_BYTES).also(random::nextBytes)
        try {
            val wrapped = Cipher.getInstance(RSA_OAEP).run {
                init(Cipher.ENCRYPT_MODE, publicKey, oaep)
                doFinal(noteKey)
            }
            val box = aesEncrypt(noteKey, plain.toByteArray(Charsets.UTF_8), NOTE_AAD)
            val out = ByteBuffer.allocate(2 + wrapped.size + box.size)
                .putShort(wrapped.size.toShort())
                .put(wrapped)
                .put(box)
                .array()
            return PREFIX + encode(out)
        } finally {
            noteKey.fill(0)
        }
    }

    // Throws when the text was not sealed with the pair of this private key or was damaged
    fun open(sealed: String, privateKey: PrivateKey): String {
        require(isSealed(sealed)) { "Not a sealed note" }
        val buffer = ByteBuffer.wrap(decode(sealed.removePrefix(PREFIX)))
        val wrapped = ByteArray(buffer.short.toInt()).also { buffer.get(it) }
        val box = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val noteKey = Cipher.getInstance(RSA_OAEP).run {
            init(Cipher.DECRYPT_MODE, privateKey, oaep)
            doFinal(wrapped)
        }
        try {
            return String(aesDecrypt(noteKey, box, NOTE_AAD), Charsets.UTF_8)
        } finally {
            noteKey.fill(0)
        }
    }

    // base64(salt | iterations | nonce | ciphertext with tag), the key is PBKDF2-HMAC-SHA256 of the password
    fun wrapWithPassword(secret: ByteArray, password: CharArray, iterations: Int): String {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val key = pbkdf2(password, salt, iterations)
        try {
            val box = aesEncrypt(key, secret, PASSWORD_AAD)
            return encode(ByteBuffer.allocate(SALT_BYTES + 4 + box.size).put(salt).putInt(iterations).put(box).array())
        } finally {
            key.fill(0)
        }
    }

    // Null when the password is wrong
    fun unwrapWithPassword(wrapped: String, password: CharArray): ByteArray? {
        val buffer = ByteBuffer.wrap(decode(wrapped))
        val salt = ByteArray(SALT_BYTES).also { buffer.get(it) }
        val iterations = buffer.int
        val box = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val key = pbkdf2(password, salt, iterations)
        return try {
            aesDecrypt(key, box, PASSWORD_AAD)
        } catch (e: GeneralSecurityException) {
            null
        } finally {
            key.fill(0)
        }
    }

    // PBKDF2WithHmacSHA256 appeared only in Android 8, the app runs on Android 6, so it is done by hand.
    // One block is enough for a 256-bit key.
    internal fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val encoded = Charsets.UTF_8.encode(CharBuffer.wrap(password))
        val passwordBytes = ByteArray(encoded.remaining()).also { encoded.get(it) }
        encoded.array().fill(0)
        try {
            val mac = Mac.getInstance(HMAC)
            mac.init(SecretKeySpec(passwordBytes, HMAC))
            mac.update(salt)
            mac.update(byteArrayOf(0, 0, 0, 1))
            var u = mac.doFinal()
            val result = u.copyOf()
            repeat(iterations - 1) {
                u = mac.doFinal(u)
                for (i in result.indices) result[i] = result[i] xor u[i]
            }
            return result
        } finally {
            passwordBytes.fill(0)
        }
    }

    // nonce | ciphertext with tag
    internal fun aesEncrypt(key: ByteArray, plain: ByteArray, aad: ByteArray): ByteArray {
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance(AES_GCM).apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            updateAAD(aad)
        }
        return nonce + cipher.doFinal(plain)
    }

    internal fun aesDecrypt(key: ByteArray, box: ByteArray, aad: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_GCM).apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, box, 0, NONCE_BYTES))
            updateAAD(aad)
        }
        return cipher.doFinal(box, NONCE_BYTES, box.size - NONCE_BYTES)
    }

    internal fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    internal fun decode(text: String): ByteArray = Base64.decode(text, Base64.NO_WRAP)
}
