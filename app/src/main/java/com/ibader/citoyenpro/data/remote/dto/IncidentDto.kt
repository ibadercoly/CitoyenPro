package com.ibader.citoyenpro.data.remote.dto

import com.google.gson.annotations.SerializedName

// photoUrl (URL distante après upload) plutôt que photoUri (chemin local
// content:// utilisé par IncidentEntity côté Room) : les deux représentent la
// photo du signalement mais aucune valeur n'est réutilisable telle quelle
// pour l'autre source, d'où le renommage explicite entre DTO et entité.
data class IncidentDto(
    val id: Long?,
    val titre: String,
    val description: String,
    @SerializedName("category_id") val categoryId: Long,
    val priority: String,
    val status: String,
    @SerializedName("photo_url") val photoUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val adresse: String,
    @SerializedName("citoyen_id") val citoyenId: Long,
    @SerializedName("service_affecte") val serviceAffecte: String?,
    @SerializedName("date_creation") val dateCreation: Long,
    @SerializedName("date_maj") val dateMaj: Long
)
