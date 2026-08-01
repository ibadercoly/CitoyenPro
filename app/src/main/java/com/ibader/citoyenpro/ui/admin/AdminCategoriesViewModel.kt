package com.ibader.citoyenpro.ui.admin

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Liste réactive des catégories (Flow Room) + formulaire d'ajout/modification
// piloté par dialogue + suppression avec confirmation. Une catégorie encore
// référencée par des signalements ne peut pas être supprimée (contrainte
// FK RESTRICT sur IncidentEntity) : l'échec est capté et remonté dans l'état
// plutôt que de laisser l'exception remonter jusqu'à l'UI.
class AdminCategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _formState = MutableStateFlow<CategoryFormState?>(null)
    private val _categoryPendingDeletion = MutableStateFlow<CategoryEntity?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdminCategoriesUiState> = combine(
        categoryRepository.getAll(),
        _formState,
        _categoryPendingDeletion,
        _errorMessage
    ) { categories, formState, pendingDeletion, errorMessage ->
        AdminCategoriesUiState(
            isLoading = false,
            categories = categories,
            formState = formState,
            categoryPendingDeletion = pendingDeletion,
            errorMessage = errorMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminCategoriesUiState())

    fun onAddClick() {
        _formState.value = CategoryFormState()
    }

    fun onEditClick(category: CategoryEntity) {
        _formState.value = CategoryFormState(id = category.id, nom = category.nom, description = category.description)
    }

    fun onFormNomChanged(nom: String) {
        _formState.update { it?.copy(nom = nom) }
    }

    fun onFormDescriptionChanged(description: String) {
        _formState.update { it?.copy(description = description) }
    }

    fun onDismissForm() {
        _formState.value = null
    }

    fun onSaveClick() {
        val form = _formState.value ?: return
        if (!form.isValid) return

        viewModelScope.launch {
            if (form.id == null) {
                categoryRepository.insert(CategoryEntity(nom = form.nom.trim(), description = form.description.trim()))
            } else {
                categoryRepository.update(
                    CategoryEntity(id = form.id, nom = form.nom.trim(), description = form.description.trim())
                )
            }
            _formState.value = null
        }
    }

    fun onDeleteClick(category: CategoryEntity) {
        _categoryPendingDeletion.value = category
    }

    fun onDismissDeleteConfirmation() {
        _categoryPendingDeletion.value = null
    }

    fun onConfirmDelete() {
        val category = _categoryPendingDeletion.value ?: return
        viewModelScope.launch {
            try {
                categoryRepository.delete(category)
            } catch (e: SQLiteConstraintException) {
                _errorMessage.value =
                    "Impossible de supprimer « ${category.nom} » : des signalements y sont encore rattachés."
            }
            _categoryPendingDeletion.value = null
        }
    }

    fun onErrorDismissed() {
        _errorMessage.value = null
    }

    companion object {
        fun factory(categoryRepository: CategoryRepository) = viewModelFactory {
            initializer { AdminCategoriesViewModel(categoryRepository) }
        }
    }
}
