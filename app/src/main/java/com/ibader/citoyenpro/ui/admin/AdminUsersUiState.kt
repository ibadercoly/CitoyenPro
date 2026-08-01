package com.ibader.citoyenpro.ui.admin

import com.ibader.citoyenpro.domain.model.UserRole

data class AdminUserListItem(
    val id: Long,
    val nom: String,
    val email: String,
    val role: UserRole
)

data class AdminUsersUiState(
    val isLoading: Boolean = true,
    val items: List<AdminUserListItem> = emptyList()
)
