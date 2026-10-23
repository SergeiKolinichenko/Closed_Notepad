package info.sergeikolinichenko.closednotepad.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NoteCipherTest {

    private val pair = NoteCipher.generateKeyPair()

    @Test
    fun sealedTextOpensBackAndHidesTheText() {
        val text = "Пин-код 1234\nWi-Fi: secret ☑"
        val sealed = NoteCipher.seal(text, pair.public)

        assertTrue(NoteCipher.isSealed(sealed))
        assertFalse(sealed.contains("1234"))
        assertEquals(text, NoteCipher.open(sealed, pair.private))
    }

    @Test
    fun sameTextIsSealedDifferentlyEachTime() {
        assertTrue(NoteCipher.seal("a", pair.public) != NoteCipher.seal("a", pair.public))
    }

    @Test(expected = Exception::class)
    fun anotherKeyCantOpen() {
        val sealed = NoteCipher.seal("text", pair.public)
        NoteCipher.open(sealed, NoteCipher.generateKeyPair().private)
    }

    @Test(expected = Exception::class)
    fun damagedTextDoesNotOpen() {
        val sealed = NoteCipher.seal("text", pair.public)
        val bytes = NoteCipher.decode(sealed.removePrefix(NoteCipher.PREFIX))
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 1).toByte()
        NoteCipher.open(NoteCipher.PREFIX + NoteCipher.encode(bytes), pair.private)
    }

    @Test
    fun passwordUnwrapsOnlyWithTheSamePassword() {
        val secret = pair.private.encoded
        val wrapped = NoteCipher.wrapWithPassword(secret, "correct horse".toCharArray(), 1_000)

        assertArrayEquals(secret, NoteCipher.unwrapWithPassword(wrapped, "correct horse".toCharArray()))
        assertNull(NoteCipher.unwrapWithPassword(wrapped, "correct hors".toCharArray()))
    }

    @Test
    fun pbkdf2MatchesTheStandardOne() {
        val password = "пароль-123".toCharArray()
        val salt = ByteArray(16) { (it * 7).toByte() }
        val expected = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password, salt, 2_000, 256)).encoded
        assertArrayEquals(expected, NoteCipher.pbkdf2(password, salt, 2_000))
    }
}
