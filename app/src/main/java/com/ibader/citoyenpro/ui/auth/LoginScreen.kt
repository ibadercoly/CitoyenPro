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
fun LoginRoute(
    userRepository: UserRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(userRepository))
) {
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) onLoginSuccess()
    }

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onLoginEmailChange,
        onPasswordChange = viewModel::onLoginPasswordChange,
        onLoginClick = viewModel::login,
        onNavigateToRegister = onNavigateToRegister,
        modifier = modifier
    )
}

// Composable "contenu" : purement déclaratif, sans dépendance au ViewModel — testable et previewable.
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(
                title = "Connexion",
                subtitle = "Connectez-vous pour signaler et suivre vos incidents"
            )

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
                text = "Se connecter",
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading,
                onClick = onLoginClick
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToRegister,
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Pas encore de compte ? S'inscrire")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Connexion — clair", showBackground = true)
@Composable
private fun LoginScreenPreview() {
    CitoyenProTheme {
        LoginScreen(
            uiState = LoginUiState(email = "citoyen@example.com"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(name = "Connexion — erreur", showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    CitoyenProTheme {
        LoginScreen(
            uiState = LoginUiState(
                email = "citoyen@example.com",
                password = "secret",
                errorMessage = "Email ou mot de passe incorrect"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(name = "Connexion — sombre, chargement", showBackground = true)
@Composable
private fun LoginScreenDarkPreview() {
    CitoyenProTheme(darkTheme = true) {
        LoginScreen(
            uiState = LoginUiState(email = "citoyen@example.com", isLoading = true),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}
