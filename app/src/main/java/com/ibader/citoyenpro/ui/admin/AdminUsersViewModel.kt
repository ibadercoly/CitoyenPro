package com.ibader.citoyenpro.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AdminUsersViewModel(userRepository: UserRepository) : ViewModel() {

    val uiState: StateFlow<AdminUsersUiState> = userRepository.getAll()
        .map { users ->
            AdminUsersUiState(
                isLoading = false,
                items = users.map { user ->
                    AdminUserListItem(id = user.id, nom = user.nom, email = user.email, role = user.role)
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminUsersUiState())

    companion object {
        fun factory(userRepository: UserRepository) = viewModelFactory {
            initializer { AdminUsersViewModel(userRepository) }
        }
    }
}
