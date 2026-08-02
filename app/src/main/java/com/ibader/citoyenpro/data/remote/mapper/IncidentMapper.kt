package com.ibader.citoyenpro.data.remote.mapper

import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.remote.dto.IncidentDto
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import java.time.Instant

fun IncidentDto.toEntity(): IncidentEntity = IncidentEntity(
    id = id ?: 0L,
    titre = titre,
    description = description,
    categoryId = categoryId,
    priority = Priority.valueOf(priority),
    status = IncidentStatus.valueOf(status),
    photoUri = photoUrl,
    latitude = latitude,
    longitude = longitude,
    adresse = adresse,
    citoyenUid = citoyenUid,
    serviceAffecte = serviceAffecte,
    dateCreation = Instant.parse(dateCreation).toEpochMilli(),
    dateMaj = Instant.parse(dateMaj).toEpochMilli()
)

// id = 0L (auto-généré, pas encore inséré en local) ne correspond à aucun id
// serveur valide : on l'omet plutôt que d'envoyer 0.
fun IncidentEntity.toDto(): IncidentDto = IncidentDto(
    id = id.takeIf { it != 0L },
    titre = titre,
    description = description,
    categoryId = categoryId,
    priority = priority.name,
    status = status.name,
    photoUrl = photoUri,
    latitude = latitude,
    longitude = longitude,
    adresse = adresse,
    citoyenUid = citoyenUid,
    serviceAffecte = serviceAffecte,
    dateCreation = Instant.ofEpochMilli(dateCreation).toString(),
    dateMaj = Instant.ofEpochMilli(dateMaj).toString()
)
