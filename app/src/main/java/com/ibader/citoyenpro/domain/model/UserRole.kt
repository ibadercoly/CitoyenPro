package com.ibader.citoyenpro.domain.model

enum class UserRole {
    CITOYEN,
    ADMIN
}

fun UserRole.libelle(): String = when (this) {
    UserRole.CITOYEN -> "Citoyen"
    UserRole.ADMIN -> "Administrateur"
}
