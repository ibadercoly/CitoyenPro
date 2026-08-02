package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.dao.CategoryDao
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.data.remote.ApiService
import com.ibader.citoyenpro.data.remote.mapper.toDto
import com.ibader.citoyenpro.data.remote.mapper.toEntity
import kotlinx.coroutines.flow.Flow

// Room reste la source de vérité affichée à l'UI ; chaque écriture est
// d'abord appliquée en local (l'UI, qui observe Room, réagit donc
// immédiatement même hors-ligne), puis répercutée vers l'API au mieux
// (best-effort, runCatching). Contrairement à IncidentRepository, pas de file
// d'attente persistante ici : les catégories changent rarement et un échec
// réseau se rattrape simplement au prochain syncFromRemote(), sans nécessiter
// de rejeu précis d'opérations.
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val apiService: ApiService
) {
    suspend fun insert(category: CategoryEntity): Long {
        val id = categoryDao.insert(category)
        runCatching { apiService.createCategory(category.copy(id = id).toDto()) }
        return id
    }

    suspend fun update(category: CategoryEntity) {
        categoryDao.update(category)
        runCatching { apiService.updateCategory(category.id, category.toDto()) }
    }

    // La suppression locale (Room) est tentée en premier : une contrainte FK
    // (catégorie encore référencée par des signalements) lève une
    // SQLiteConstraintException avant tout appel réseau, exactement comme
    // avant l'introduction de l'API (cf. AdminCategoriesViewModel).
    suspend fun delete(category: CategoryEntity) {
        categoryDao.delete(category)
        runCatching { apiService.deleteCategory(category.id) }
    }

    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)

    fun getAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    // Récupère les catégories distantes et les fusionne en local ; à appeler
    // au lancement de l'app (cf. AppNavHost). Silencieux en cas d'échec (pas
    // de réseau, backend indisponible) : Room reste alors la seule source de
    // données. Met à jour la ligne existante plutôt que de la remplacer
    // (delete + insert) : un remplacement échouerait avec une
    // SQLiteConstraintException (et plantait toute l'app, cette synchro
    // n'étant pas protégée individuellement) dès qu'un signalement local
    // référence encore cette catégorie, la contrainte de clé étrangère
    // interdisant sa suppression tant qu'elle est utilisée.
    suspend fun syncFromRemote() {
        val remoteCategories = runCatching { apiService.getCategories() }.getOrNull() ?: return
        remoteCategories.forEach { dto ->
            runCatching {
                val entity = dto.toEntity()
                if (categoryDao.getById(entity.id) != null) {
                    categoryDao.update(entity)
                } else {
                    categoryDao.insert(entity)
                }
            }
        }
    }
}
