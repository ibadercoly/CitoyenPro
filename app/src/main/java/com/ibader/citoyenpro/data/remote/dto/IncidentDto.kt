package com.ibader.citoyenpro.data.remote.dto

// Reflète exactement le JSON renvoyé par le backend (Prisma sérialise ses
// champs tels que déclarés dans le schéma, en camelCase — pas de snake_case,
// donc aucun @SerializedName n'est nécessaire ici). photoUrl (URL distante
// après upload) plutôt que photoUri (chemin local content:// utilisé par
// IncidentEntity côté Room) : les deux représentent la photo du signalement
// mais aucune valeur n'est réutilisable telle quelle pour l'autre source,
// d'où le renommage explicite entre DTO et entité. citoyenUid identifie
// l'auteur par son uid Firebase, jamais par un id numérique. dateCreation/
// dateMaj sont des dates ISO-8601 (sérialisation Prisma), pas des epoch millis.
data class IncidentDto(
    val id: Long?,
    val titre: String,
    val description: String,
    val categoryId: Long,
    val priority: String,
    val status: String,
    val photoUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val adresse: String,
    val citoyenUid: String,
    val serviceAffecte: String?,
    val dateCreation: String,
    val dateMaj: String
)
