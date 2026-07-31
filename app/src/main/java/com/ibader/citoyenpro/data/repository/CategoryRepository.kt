package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.dao.CategoryDao
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

// S'appuie uniquement sur Room pour l'instant ; une source réseau (Retrofit)
// sera introduite plus tard pour synchroniser les catégories avec le backend.
class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    suspend fun insert(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)

    fun getAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()
}
