package com.ibader.citoyenpro.data.remote

import com.ibader.citoyenpro.data.remote.dto.CategoryDto
import com.ibader.citoyenpro.data.remote.dto.IncidentDto
import com.ibader.citoyenpro.data.remote.dto.SyncUserRequestDto
import com.ibader.citoyenpro.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Contrat HTTP avec le backend (cf. RetrofitClient pour l'URL de
// développement). Fonctions suspend (support natif Retrofit) ; une réponse
// en erreur lève une HttpException, un souci réseau une IOException — à
// charge du repository appelant de les traduire en erreurs métier, comme il
// le fait déjà pour Room (EmailAlreadyUsedException, etc.).
interface ApiService {

    // --- Utilisateurs ---
    // Pas de login/register ici : l'authentification est entièrement gérée
    // par Firebase Auth côté client (cf. UserRepository). Le backend ne fait
    // que vérifier le token Firebase (header Authorization) et synchroniser
    // le profil applicatif correspondant.

    @POST("users/sync")
    suspend fun syncUser(@Body request: SyncUserRequestDto): UserDto

    @GET("users/me")
    suspend fun getMe(): UserDto

    // --- Catégories ---

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>

    @POST("categories")
    suspend fun createCategory(@Body category: CategoryDto): CategoryDto

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body category: CategoryDto): CategoryDto

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)

    // --- Signalements ---

    @GET("incidents")
    suspend fun getIncidents(): List<IncidentDto>

    @GET("incidents/{id}")
    suspend fun getIncident(@Path("id") id: Long): IncidentDto

    @POST("incidents")
    suspend fun createIncident(@Body incident: IncidentDto): IncidentDto

    @PUT("incidents/{id}")
    suspend fun updateIncident(@Path("id") id: Long, @Body incident: IncidentDto): IncidentDto

    @DELETE("incidents/{id}")
    suspend fun deleteIncident(@Path("id") id: Long)
}
