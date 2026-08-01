package com.ibader.citoyenpro.data.remote.mapper

import com.ibader.citoyenpro.data.local.entities.UserEntity
import com.ibader.citoyenpro.data.remote.dto.UserDto
import com.ibader.citoyenpro.domain.model.UserRole

// Le backend ne renvoie et n'attend jamais de hash de mot de passe : c'est
// une donnée strictement locale (déjà connue si l'utilisateur existe en
// base, à fournir explicitement par l'appelant sinon — ex. hash du mot de
// passe saisi au login, pour retrouver la session hors-ligne la prochaine fois).
fun UserDto.toEntity(motDePasseHash: String): UserEntity = UserEntity(
    id = id ?: 0L,
    nom = nom,
    email = email,
    motDePasseHash = motDePasseHash,
    role = UserRole.valueOf(role)
)

fun UserEntity.toDto(): UserDto = UserDto(
    id = id.takeIf { it != 0L },
    nom = nom,
    email = email,
    role = role.name
)
