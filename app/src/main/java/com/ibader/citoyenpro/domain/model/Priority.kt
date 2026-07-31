package com.ibader.citoyenpro.domain.model

enum class Priority {
    BASSE,
    MOYENNE,
    HAUTE,
    URGENTE
}

fun Priority.libelle(): String = when (this) {
    Priority.BASSE -> "Basse"
    Priority.MOYENNE -> "Moyenne"
    Priority.HAUTE -> "Haute"
    Priority.URGENTE -> "Urgente"
}
