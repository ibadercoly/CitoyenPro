package com.ibader.citoyenpro.data.remote.mapper

import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.data.remote.dto.CategoryDto

fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = id ?: 0L,
    nom = nom,
    description = description
)

// id = 0L (auto-généré, pas encore inséré en local) ne correspond à aucun id
// serveur valide : on l'omet plutôt que d'envoyer 0.
fun CategoryEntity.toDto(): CategoryDto = CategoryDto(
    id = id.takeIf { it != 0L },
    nom = nom,
    description = description
)
