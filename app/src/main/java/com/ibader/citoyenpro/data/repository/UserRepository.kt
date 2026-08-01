package com.ibader.citoyenpro.data.repository

import com.ibader.citoyenpro.data.local.dao.UserDao
import com.ibader.citoyenpro.data.local.entities.UserEntity
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.util.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmailAlreadyUsedException : Exception("Un compte existe déjà avec cet email")
class InvalidCredentialsException : Exception("Email ou mot de passe incorrect")

// S'appuie uniquement sur Room pour l'instant ; une source réseau (Retrofit)
// sera introduite plus tard pour synchroniser les utilisateurs avec le backend.
class UserRepository(
    private val userDao: UserDao
) {
    // Session courante conservée en mémoire (perdue si le processus est tué) :
    // pas de persistance sur disque tant qu'aucun mécanisme de token/refresh n'existe.
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    suspend fun insert(user: UserEntity): Long = userDao.insert(user)

    suspend fun update(user: UserEntity) = userDao.update(user)

    suspend fun delete(user: UserEntity) = userDao.delete(user)

    suspend fun getById(id: Long): UserEntity? = userDao.getById(id)

    suspend fun getByEmail(email: String): UserEntity? = userDao.getByEmail(email)

    fun getAll(): Flow<List<UserEntity>> = userDao.getAll()

    // Crée un compte citoyen (rôle par défaut) avec un mot de passe hashé,
    // puis ouvre la session sur ce nouvel utilisateur.
    suspend fun register(nom: String, email: String, password: String): Result<UserEntity> = runCatching {
        if (userDao.getByEmail(email) != null) throw EmailAlreadyUsedException()

        val newUser = UserEntity(
            nom = nom,
            email = email,
            motDePasseHash = PasswordHasher.hash(password),
            role = UserRole.CITOYEN
        )
        val id = userDao.insert(newUser)
        val created = newUser.copy(id = id)
        _currentUser.value = created
        created
    }

    // Vérifie l'email et le mot de passe, puis ouvre la session en cas de succès.
    suspend fun login(email: String, password: String): Result<UserEntity> = runCatching {
        val user = userDao.getByEmail(email) ?: throw InvalidCredentialsException()
        if (!PasswordHasher.verify(password, user.motDePasseHash)) throw InvalidCredentialsException()
        _currentUser.value = user
        user
    }

    fun logout() {
        _currentUser.value = null
    }

    // Met à jour la session en cours si le citoyen crédité est celui
    // actuellement connecté, pour que l'UI (profil, badges) réagisse tout de
    // suite sans attendre une relecture explicite de Room.
    suspend fun addPoints(userId: Long, amount: Int) {
        val user = userDao.getById(userId) ?: return
        val updated = user.copy(points = user.points + amount)
        userDao.update(updated)
        if (_currentUser.value?.id == userId) {
            _currentUser.value = updated
        }
    }
}
