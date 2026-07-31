package com.ibader.citoyenpro.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Hachage PBKDF2WithHmacSHA1 (garanti disponible dès l'API 24, contrairement à
// PBKDF2WithHmacSHA256 qui ne l'est qu'à partir de l'API 26) avec sel aléatoire
// par mot de passe et itérations élevées : remplace l'ancien SHA-256 sans sel,
// vulnérable aux tables arc-en-ciel et au brute-force. Le sel et le nombre
// d'itérations sont encodés avec le hash pour rester vérifiables si ces
// paramètres évoluent plus tard.
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    suspend fun hash(motDePasse: String): String = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = deriveKey(motDePasse, salt, ITERATIONS)
        "$ITERATIONS:${salt.toBase64()}:${hash.toBase64()}"
    }

    suspend fun verify(motDePasse: String, encoded: String): Boolean = withContext(Dispatchers.Default) {
        val parts = encoded.split(":")
        if (parts.size != 3) return@withContext false
        val iterations = parts[0].toIntOrNull() ?: return@withContext false
        val salt = parts[1].fromBase64() ?: return@withContext false
        val expected = parts[2].fromBase64() ?: return@withContext false
        val actual = deriveKey(motDePasse, salt, iterations)
        MessageDigest.isEqual(actual, expected)
    }

    private fun deriveKey(motDePasse: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(motDePasse.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray? =
        runCatching { Base64.decode(this, Base64.NO_WRAP) }.getOrNull()
}
