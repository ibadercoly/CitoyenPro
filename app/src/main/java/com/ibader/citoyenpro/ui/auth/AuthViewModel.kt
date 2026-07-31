package com.ibader.citoyenpro.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.local.entities.UserEntity
import com.ibader.citoyenpro.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private const val PASSWORD_MIN_LENGTH = 6

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    // Session courante (utilisateur connecté), conservée en mémoire par le repository
    // et partagée avec le reste de l'application (routage citoyen/admin, etc.).
    val currentUser: StateFlow<UserEntity?> = userRepository.currentUser

    // --- Connexion ---

    fun onLoginEmailChange(email: String) {
        _loginUiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onLoginPasswordChange(password: String) {
        _loginUiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun login() {
        val state = _loginUiState.value
        val emailError = validateEmail(state.email)
        val passwordError = if (state.password.isBlank()) "Le mot de passe est requis" else null

        if (emailError != null || passwordError != null) {
            _loginUiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            userRepository.login(state.email.trim(), state.password)
                .onSuccess {
                    _loginUiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                }
                .onFailure { error ->
                    _loginUiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Une erreur est survenue")
                    }
                }
        }
    }

    // --- Inscription ---

    fun onRegisterNomChange(nom: String) {
        _registerUiState.update { it.copy(nom = nom, nomError = null, errorMessage = null) }
    }

    fun onRegisterEmailChange(email: String) {
        _registerUiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onRegisterPasswordChange(password: String) {
        _registerUiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onRegisterConfirmPasswordChange(confirmPassword: String) {
        _registerUiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null, errorMessage = null) }
    }

    fun register() {
        val state = _registerUiState.value
        val nomError = if (state.nom.isBlank()) "Le nom est requis" else null
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmPasswordError = if (state.confirmPassword != state.password) {
            "Les mots de passe ne correspondent pas"
        } else null

        if (nomError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _registerUiState.update {
                it.copy(
                    nomError = nomError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _registerUiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            userRepository.register(state.nom.trim(), state.email.trim(), state.password)
                .onSuccess {
                    _registerUiState.update { it.copy(isLoading = false, isRegisterSuccessful = true) }
                }
                .onFailure { error ->
                    _registerUiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Une erreur est survenue")
                    }
                }
        }
    }

    fun logout() {
        userRepository.logout()
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "L'email est requis"
        !EMAIL_REGEX.matches(email.trim()) -> "Format d'email invalide"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Le mot de passe est requis"
        password.length < PASSWORD_MIN_LENGTH -> "Le mot de passe doit contenir au moins $PASSWORD_MIN_LENGTH caractères"
        else -> null
    }

    companion object {
        fun factory(userRepository: UserRepository) = viewModelFactory {
            initializer { AuthViewModel(userRepository) }
        }
    }
}
