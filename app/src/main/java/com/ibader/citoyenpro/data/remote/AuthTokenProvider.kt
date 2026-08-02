package com.ibader.citoyenpro.data.remote

// Détient le token ID Firebase en mémoire pour AuthInterceptor. Alimenté en
// continu par UserRepository (listener Firebase addIdTokenListener) : connexion,
// déconnexion, et rafraîchissements automatiques du SDK toutes les ~heures.
class AuthTokenProvider {
    @Volatile
    var token: String? = null
}
