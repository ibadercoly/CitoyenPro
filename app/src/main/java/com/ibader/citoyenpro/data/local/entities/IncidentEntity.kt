package com.ibader.citoyenpro.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority

@Entity(
    tableName = "incidents",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["citoyenId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["citoyenId"]),
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
    val citoyenId: Long,
    @ColumnInfo(name = "service_affecte")
    val serviceAffecte: String? = null,
    @ColumnInfo(name = "date_creation")
    val dateCreation: Long,
    @ColumnInfo(name = "date_maj")
    val dateMaj: Long
)
