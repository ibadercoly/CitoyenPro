package com.ibader.citoyenpro.data.remote.mapper

import com.ibader.citoyenpro.data.local.entities.UserEntity
import com.ibader.citoyenpro.data.remote.dto.UserDto
import com.ibader.citoyenpro.domain.model.UserRole

// Le backend ne connaît pas le firebaseUid d'un profil qu'il ne stocke pas
// encore : c'est une donnée strictement locale, à fournir explicitement par
// l'appelant (ex. l'uid du compte Firebase actuellement connecté).
fun UserDto.toEntity(firebaseUid: String): UserEntity = UserEntity(
    id = id ?: 0L,
    firebaseUid = firebaseUid,
    nom = nom,
    email = email,
    role = UserRole.valueOf(role)
)

fun UserEntity.toDto(): UserDto = UserDto(
    id = id.takeIf { it != 0L },
    nom = nom,
    email = email,
    role = role.name
)
