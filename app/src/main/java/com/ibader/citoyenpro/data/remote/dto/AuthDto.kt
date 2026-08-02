package com.ibader.citoyenpro.data.remote.dto

// POST /users/sync - l'uid et l'email viennent du token Firebase vérifié côté
// serveur ; seul le nom est utile ici, en secours si le token Firebase n'a
// pas de "displayName" (cf. UserRepository.syncUserWithBackend).
data class SyncUserRequestDto(
    val nom: String?
)
