package com.ibader.citoyenpro.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val nom: String,
    val email: String,
    val password: String
)

// Le backend gère l'authentification par token (jamais de mot de passe/hash
// dans la réponse) ; le token est conservé en mémoire côté client
// (RetrofitClient.authTokenProvider) et rejoué sur chaque requête par AuthInterceptor.
data class AuthResponseDto(
    val token: String,
    val user: UserDto
)
