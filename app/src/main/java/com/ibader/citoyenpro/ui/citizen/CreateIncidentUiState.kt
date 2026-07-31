package com.ibader.citoyenpro.ui.citizen

import android.net.Uri
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.domain.model.Priority

data class CreateIncidentUiState(
    val titre: String = "",
    val description: String = "",
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val priority: Priority = Priority.MOYENNE,
    val photoUri: Uri? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val adresse: String = "",
    val isLocating: Boolean = false,
    val locationError: String? = null,
    val titreError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitSuccessful: Boolean = false
) {
    val selectedCategory: CategoryEntity?
        get() = categories.find { it.id == selectedCategoryId }
}
