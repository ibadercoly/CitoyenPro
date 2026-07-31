package com.ibader.citoyenpro.util

import java.security.MessageDigest

// Hachage simple (SHA-256, sans sel) utilisé uniquement pour le compte de test
// pré-rempli en base. À remplacer par un hachage adapté aux mots de passe
// (BCrypt, Argon2, PBKDF2 avec sel) avant toute mise en production.
object PasswordHasher {
    fun hash(motDePasse: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(motDePasse.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
