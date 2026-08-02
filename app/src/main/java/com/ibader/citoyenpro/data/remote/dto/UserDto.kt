package com.ibader.citoyenpro.data.remote.dto

// Reflète exactement la réponse du backend (users/sync, users/me) : le
// serveur identifie un utilisateur par son uid Firebase, jamais par un id
// numérique.
data class UserDto(
    val uid: String,
    val nom: String,
    val email: String,
    val role: String
)
