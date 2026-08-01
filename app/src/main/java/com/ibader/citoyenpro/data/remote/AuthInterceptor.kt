package com.ibader.citoyenpro.data.remote

import okhttp3.Interceptor
import okhttp3.Response

// Ajoute l'en-tête Authorization sur chaque requête si une session est
// ouverte ; laisse la requête inchangée sinon (les endpoints d'auth
// eux-mêmes n'ont pas besoin de token).
class AuthInterceptor(
    private val tokenProvider: AuthTokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.token
        val original = chain.request()
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
