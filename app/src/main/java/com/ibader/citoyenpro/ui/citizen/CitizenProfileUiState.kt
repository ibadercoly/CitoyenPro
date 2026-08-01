package com.ibader.citoyenpro.ui.citizen

import com.ibader.citoyenpro.domain.model.UserRole

data class CitizenProfileUiState(
    val isLoading: Boolean = true,
    val nom: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CITOYEN,
    val totalIncidents: Int = 0
)
