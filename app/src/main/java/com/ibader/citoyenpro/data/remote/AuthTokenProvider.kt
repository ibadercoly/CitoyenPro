package com.ibader.citoyenpro.data.remote

// Détient le token de session en mémoire pour AuthInterceptor. Alimenté par
// UserRepository après un login/register réussi côté API (le repository ne
// s'appuie que sur Room pour l'instant, cf. son commentaire d'en-tête) ; vidé
// à la déconnexion.
class AuthTokenProvider {
    @Volatile
    var token: String? = null
}
