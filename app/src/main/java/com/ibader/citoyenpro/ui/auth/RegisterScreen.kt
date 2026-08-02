package com.ibader.citoyenpro.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.ui.common.AppBackground
import com.ibader.citoyenpro.ui.common.AppPrimaryButton
import com.ibader.citoyenpro.ui.common.AppTextField
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

// Composable "route" : instancie l'AuthViewModel (via son repository) et réagit au succès.
@Composable
fun RegisterRoute(
    userRepository: UserRepository,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(userRepository))
) {
    val uiState by viewModel.registerUiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isRegisterSuccessful) {
        if (uiState.isRegisterSuccessful) onRegisterSuccess()
    }

    RegisterScreen(
        uiState = uiState,
        onNomChange = viewModel::onRegisterNomChange,
        onEmailChange = viewModel::onRegisterEmailChange,
        onPasswordChange = viewModel::onRegisterPasswordChange,
        onConfirmPasswordChange = viewModel::onRegisterConfirmPasswordChange,
        onRegisterClick = viewModel::register,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onNomChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(
                title = "Créer un compte",
                subtitle = "Inscrivez-vous pour signaler des incidents dans votre commune"
            )

            AppTextField(
                value = uiState.nom,
                onValueChange = onNomChange,
                label = "Nom complet",
                leadingIcon = Icons.Filled.Person,
                isError = uiState.nomError != null,
                errorText = uiState.nomError,
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                leadingIcon = Icons.Filled.Email,
                isError = uiState.emailError != null,
                errorText = uiState.emailError,
                keyboardType = KeyboardType.Email,
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Mot de passe",
                leadingIcon = Icons.Filled.Lock,
                isError = uiState.passwordError != null,
                errorText = uiState.passwordError,
                keyboardType = KeyboardType.Password,
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (isPasswordVisible) {
                                "Masquer le mot de passe"
                            } else {
                                "Afficher le mot de passe"
                            }
                        )
                    }
                },
                enabled = !uiState.isLoading
            )
            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirmer le mot de passe",
                leadingIcon = Icons.Filled.Lock,
                isError = uiState.confirmPasswordError != null,
                errorText = uiState.confirmPasswordError,
                keyboardType = KeyboardType.Password,
                visualTransformation = if (isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (isConfirmPasswordVisible) {
                                "Masquer le mot de passe"
                            } else {
                                "Afficher le mot de passe"
                            }
                        )
                    }
                },
                enabled = !uiState.isLoading
            )

            uiState.errorMessage?.let { message ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            AppPrimaryButton(
                text = "S'inscrire",
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading,
                onClick = onRegisterClick
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Déjà un compte ? Se connecter")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Inscription — clair", showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    CitoyenProTheme {
        RegisterScreen(
            uiState = RegisterUiState(nom = "Amina Bader", email = "amina@example.com"),
            onNomChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Inscription — erreurs de validation", showBackground = true)
@Composable
private fun RegisterScreenErrorPreview() {
    CitoyenProTheme {
        RegisterScreen(
            uiState = RegisterUiState(
                email = "pas-un-email",
                password = "123",
                confirmPassword = "1234",
                emailError = "Format d'email invalide",
                passwordError = "Le mot de passe doit contenir au moins 6 caractères",
                confirmPasswordError = "Les mots de passe ne correspondent pas"
            ),
            onNomChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Inscription — sombre, chargement", showBackground = true)
@Composable
private fun RegisterScreenDarkPreview() {
    CitoyenProTheme(darkTheme = true) {
        RegisterScreen(
            uiState = RegisterUiState(nom = "Amina Bader", email = "amina@example.com", isLoading = true),
            onNomChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}
