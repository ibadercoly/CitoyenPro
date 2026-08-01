package com.ibader.citoyenpro.data.remote

import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Backend pas encore déployé : URL de développement local (10.0.2.2 = hôte
// machine vu depuis l'émulateur Android) à remplacer par l'URL de production
// le moment venu.
private const val BASE_URL = "http://10.0.2.2:8080/api/"

// Point d'entrée unique vers ApiService : construit paresseusement un client
// OkHttp (auth + logs) et un Retrofit (Gson) une seule fois, comme
// AppDatabase.getInstance côté Room. authTokenProvider est exposé pour que
// UserRepository puisse y déposer le token après un login réussi.
object RetrofitClient {
    val authTokenProvider = AuthTokenProvider()

    @Volatile
    private var apiService: ApiService? = null

    fun getApiService(): ApiService =
        apiService ?: synchronized(this) {
            apiService ?: buildApiService().also { apiService = it }
        }

    private fun buildApiService(): ApiService {
        // Le corps des requêtes/réponses est utile en développement pour
        // déboguer les échanges JSON ; le header Authorization est masqué
        // pour ne jamais faire fuiter un token dans les logs.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authTokenProvider))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder().create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
