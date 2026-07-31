package com.ibader.citoyenpro.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ibader.citoyenpro.domain.model.UserRole

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nom: String,
    val email: String,
    @ColumnInfo(name = "mot_de_passe_hash")
    val motDePasseHash: String,
    val role: UserRole
)
