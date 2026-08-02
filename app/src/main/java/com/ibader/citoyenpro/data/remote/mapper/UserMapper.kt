package com.ibader.citoyenpro.data.remote.mapper

import com.ibader.citoyenpro.data.local.entities.UserEntity
import com.ibader.citoyenpro.data.remote.dto.UserDto
import com.ibader.citoyenpro.domain.model.UserRole

// localId : id Room existant à conserver (0L pour une insertion), le backend
// ne connaît pas cet identifiant local et ne le renvoie jamais.
fun UserDto.toEntity(localId: Long = 0L): UserEntity = UserEntity(
    id = localId,
    firebaseUid = uid,
    nom = nom,
    email = email,
    role = UserRole.valueOf(role)
)

fun UserEntity.toDto(): UserDto = UserDto(
    uid = firebaseUid,
    nom = nom,
    email = email,
    role = role.name
)
