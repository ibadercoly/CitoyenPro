package com.ibader.citoyenpro.ui.admin

import com.ibader.citoyenpro.data.local.entities.CategoryEntity

// null id = création, id renseigné = modification d'une catégorie existante.
data class CategoryFormState(
    val id: Long? = null,
    val nom: String = "",
    val description: String = ""
) {
    val isValid: Boolean get() = nom.isNotBlank()
}

data class AdminCategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<CategoryEntity> = emptyList(),
    val formState: CategoryFormState? = null,
    val categoryPendingDeletion: CategoryEntity? = null,
    val errorMessage: String? = null
)
