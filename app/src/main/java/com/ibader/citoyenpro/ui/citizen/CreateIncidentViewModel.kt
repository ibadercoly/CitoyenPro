package com.ibader.citoyenpro.ui.citizen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity
import com.ibader.citoyenpro.data.repository.CategoryRepository
import com.ibader.citoyenpro.data.repository.IncidentRepository
import com.ibader.citoyenpro.data.repository.IncidentStatusHistoryRepository
import com.ibader.citoyenpro.data.repository.LocationRepository
import com.ibader.citoyenpro.data.repository.UserRepository
import com.ibader.citoyenpro.domain.model.CitizenPointsRules
import com.ibader.citoyenpro.domain.model.IncidentStatus
import com.ibader.citoyenpro.domain.model.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TITRE_MIN_LENGTH = 5
private const val DESCRIPTION_MIN_LENGTH = 10

class CreateIncidentViewModel(
    private val incidentRepository: IncidentRepository,
    private val incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
    categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateIncidentUiState())
    val uiState: StateFlow<CreateIncidentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onTitreChange(titre: String) {
        _uiState.update { it.copy(titre = titre, titreError = null, errorMessage = null) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null, errorMessage = null) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, categoryError = null, errorMessage = null) }
    }

    fun onPrioritySelected(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun fetchLocation() {
        _uiState.update { it.copy(isLocating = true, locationError = null) }
        viewModelScope.launch {
            val position = locationRepository.getCurrentPosition()
            _uiState.update {
                if (position != null) {
                    // Le géocodage inverse (résolution en adresse lisible) peut
                    // échouer indépendamment de la position GPS elle-même
                    // (connexion instable, service indisponible...) : on retombe
                    // alors sur les coordonnées formatées plutôt que de laisser
                    // l'adresse vide, qui bloquerait sinon l'envoi (le backend
                    // exige une adresse non vide).
                    val adresse = position.adresse
                        ?: "Position GPS : %.5f, %.5f".format(position.latitude, position.longitude)
                    it.copy(
                        isLocating = false,
                        latitude = position.latitude,
                        longitude = position.longitude,
                        adresse = adresse
                    )
                } else {
                    it.copy(isLocating = false, locationError = "Impossible de récupérer la position actuelle")
                }
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        // Garde-fou contre le double-tap : un appui rapide et répété peut
        // envoyer plusieurs clics avant que la recomposition ne désactive
        // visuellement le bouton (isLoading), ce qui créait des signalements
        // en double. Le contrôle ici est synchrone et immédiat, indépendant
        // du délai de recomposition Compose.
        if (state.isLoading) return

        val titreError = validateTitre(state.titre)
        val descriptionError = validateDescription(state.description)
        val categoryError = if (state.selectedCategoryId == null) "Sélectionnez une catégorie" else null
        // Le backend exige une adresse non vide (cf. POST /incidents) : sans
        // cette vérification, un signalement envoyé sans avoir renseigné sa
        // position reste bloqué indéfiniment dans la file de synchro,
        // rejeté en boucle silencieuse par le serveur (400), sans jamais
        // remonter d'erreur visible à l'utilisateur.
        val locationError = if (state.adresse.isBlank()) {
            "Indiquez votre position avant d'envoyer le signalement"
        } else {
            null
        }

        if (titreError != null || descriptionError != null || categoryError != null || locationError != null) {
            _uiState.update {
                it.copy(
                    titreError = titreError,
                    descriptionError = descriptionError,
                    categoryError = categoryError,
                    locationError = locationError
                )
            }
            return
        }

        val citoyen = userRepository.currentUser.value
        if (citoyen == null) {
            _uiState.update { it.copy(errorMessage = "Vous devez être connecté pour signaler un incident") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                val now = System.currentTimeMillis()
                val incidentId = incidentRepository.insert(
                    IncidentEntity(
                        titre = state.titre.trim(),
                        description = state.description.trim(),
                        categoryId = state.selectedCategoryId!!,
                        priority = state.priority,
                        status = IncidentStatus.RECU,
                        photoUri = state.photoUri?.toString(),
                        // Position par défaut si l'utilisateur n'a pas (pu) géolocaliser
                        // le signalement — la capture GPS reste optionnelle à l'envoi.
                        latitude = state.latitude ?: 0.0,
                        longitude = state.longitude ?: 0.0,
                        adresse = state.adresse,
                        citoyenUid = citoyen.firebaseUid,
                        dateCreation = now,
                        dateMaj = now
                    )
                )
                incidentStatusHistoryRepository.insert(
                    IncidentStatusHistoryEntity(
                        incidentId = incidentId,
                        status = IncidentStatus.RECU,
                        date = now
                    )
                )
                userRepository.addPoints(citoyen.firebaseUid, CitizenPointsRules.NOUVEAU_SIGNALEMENT)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSubmitSuccessful = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Une erreur est survenue")
                }
            }
        }
    }

    private fun validateTitre(titre: String): String? = when {
        titre.isBlank() -> "Le titre est requis"
        titre.trim().length < TITRE_MIN_LENGTH -> "Le titre doit contenir au moins $TITRE_MIN_LENGTH caractères"
        else -> null
    }

    private fun validateDescription(description: String): String? = when {
        description.isBlank() -> "La description est requise"
        description.trim().length < DESCRIPTION_MIN_LENGTH ->
            "La description doit contenir au moins $DESCRIPTION_MIN_LENGTH caractères"
        else -> null
    }

    companion object {
        fun factory(
            incidentRepository: IncidentRepository,
            incidentStatusHistoryRepository: IncidentStatusHistoryRepository,
            categoryRepository: CategoryRepository,
            userRepository: UserRepository,
            locationRepository: LocationRepository
        ) = viewModelFactory {
            initializer {
                CreateIncidentViewModel(
                    incidentRepository,
                    incidentStatusHistoryRepository,
                    categoryRepository,
                    userRepository,
                    locationRepository
                )
            }
        }
    }
}
