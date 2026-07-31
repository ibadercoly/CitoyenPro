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
                    it.copy(
                        isLocating = false,
                        latitude = position.latitude,
                        longitude = position.longitude,
                        adresse = position.adresse ?: it.adresse
                    )
                } else {
                    it.copy(isLocating = false, locationError = "Impossible de récupérer la position actuelle")
                }
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        val titreError = validateTitre(state.titre)
        val descriptionError = validateDescription(state.description)
        val categoryError = if (state.selectedCategoryId == null) "Sélectionnez une catégorie" else null

        if (titreError != null || descriptionError != null || categoryError != null) {
            _uiState.update {
                it.copy(titreError = titreError, descriptionError = descriptionError, categoryError = categoryError)
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
                        citoyenId = citoyen.id,
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
