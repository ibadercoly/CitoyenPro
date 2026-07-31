package com.ibader.citoyenpro.data.local.database

import androidx.room.TypeConverter
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import com.ibader.citoyenpro.domain.model.UserRole

// Room 2.3+ sait déjà convertir les enums nativement (via leur nom), mais on les
// déclare explicitement pour garder ce mapping lisible et maîtrisé dans le temps.
// Les dates (dateCreation, dateMaj) sont stockées directement en Long (epoch millis)
// dans les entités : aucun convertisseur n'est donc nécessaire pour elles.
class Converters {

    @TypeConverter
    fun fromIncidentStatus(status: IncidentStatus): String = status.name

    @TypeConverter
    fun toIncidentStatus(value: String): IncidentStatus = IncidentStatus.valueOf(value)

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)
}
