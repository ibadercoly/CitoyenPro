package com.ibader.citoyenpro.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority

// citoyenUid identifie l'auteur par son uid Firebase (comme le backend),
// sans clé étrangère vers `users` : l'app ne connaît pas forcément le profil
// Room des autres citoyens (incidents reçus du serveur), qui ne stocke que le
// citoyen actuellement connecté sur cet appareil.
@Entity(
    tableName = "incidents",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["citoyenUid"]),
        Index(value = ["status"])
    ]
)
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val titre: String,
    val description: String,
    val categoryId: Long,
    val priority: Priority,
    val status: IncidentStatus,
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,
    val latitude: Double,
    val longitude: Double,
    val adresse: String,
    val citoyenUid: String,
    @ColumnInfo(name = "service_affecte")
    val serviceAffecte: String? = null,
    @ColumnInfo(name = "date_creation")
    val dateCreation: Long,
    @ColumnInfo(name = "date_maj")
    val dateMaj: Long
)
