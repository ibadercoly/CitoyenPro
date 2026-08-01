package com.ibader.citoyenpro.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ibader.citoyenpro.domain.model.UserRole

// Les identifiants (email/mot de passe) sont gérés par Firebase Authentication ;
// firebaseUid relie ce profil applicatif (rôle, nom, points) au compte Firebase.
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["firebase_uid"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String,
    val nom: String,
    val email: String,
    val role: UserRole,
    val points: Int = 0
)
