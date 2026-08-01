package com.ibader.citoyenpro.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.messaging.FirebaseMessaging
import com.ibader.citoyenpro.data.local.dao.UserDao
import com.ibader.citoyenpro.data.local.entities.UserEntity
import com.ibader.citoyenpro.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class EmailAlreadyUsedException : Exception("Un compte existe déjà avec cet email")
class InvalidCredentialsException : Exception("Email ou mot de passe incorrect")

// L'authentification (création de compte, connexion, hachage du mot de passe) est
// déléguée à Firebase Authentication. Room ne conserve que le profil applicatif
// (rôle citoyen/admin, nom, points) rattaché au compte Firebase via firebaseUid.
class UserRepository(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) {
    // Session courante : reflète le profil Room associé à l'utilisateur Firebase
    // connecté. Contrairement à l'ancienne session en mémoire, Firebase persiste
    // lui-même la connexion sur disque, donc restoreSession() permet de la
    // retrouver après un redémarrage du process (cf. AppNavHost au démarrage).
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                _currentUser.value = null
            }
        }
    }

    suspend fun insert(user: UserEntity): Long = userDao.insert(user)

    suspend fun update(user: UserEntity) = userDao.update(user)

    suspend fun delete(user: UserEntity) = userDao.delete(user)

    suspend fun getById(id: Long): UserEntity? = userDao.getById(id)

    suspend fun getByEmail(email: String): UserEntity? = userDao.getByEmail(email)

    fun getAll(): Flow<List<UserEntity>> = userDao.getAll()

    // A appeler une fois au démarrage de l'app : si Firebase a déjà une session
    // ouverte (persistée par son SDK), recharge le profil Room correspondant.
    suspend fun restoreSession() {
        val firebaseUser = firebaseAuth.currentUser ?: return
        _currentUser.value = userDao.getByFirebaseUid(firebaseUser.uid)
        subscribeToUserTopic(firebaseUser.uid)
    }

    // Crée le compte Firebase, puis le profil citoyen (rôle par défaut) associé
    // en Room, et ouvre la session sur ce nouvel utilisateur.
    suspend fun register(nom: String, email: String, password: String): Result<UserEntity> = runCatching {
        val authResult = try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            throw EmailAlreadyUsedException()
        }
        val firebaseUser = authResult.user ?: throw InvalidCredentialsException()

        val newUser = UserEntity(
            firebaseUid = firebaseUser.uid,
            nom = nom,
            email = email,
            role = UserRole.CITOYEN
        )
        val id = userDao.insert(newUser)
        val created = newUser.copy(id = id)
        _currentUser.value = created
        subscribeToUserTopic(firebaseUser.uid)
        created
    }

    // Authentifie auprès de Firebase, puis charge le profil applicatif (rôle,
    // points) correspondant en Room, et ouvre la session en cas de succès.
    suspend fun login(email: String, password: String): Result<UserEntity> = runCatching {
        val authResult = try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        } catch (e: FirebaseAuthInvalidUserException) {
            throw InvalidCredentialsException()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw InvalidCredentialsException()
        }
        val firebaseUser = authResult.user ?: throw InvalidCredentialsException()
        val user = userDao.getByFirebaseUid(firebaseUser.uid) ?: throw InvalidCredentialsException()
        _currentUser.value = user
        subscribeToUserTopic(firebaseUser.uid)
        user
    }

    suspend fun logout() {
        val firebaseUid = _currentUser.value?.firebaseUid
        firebaseAuth.signOut()
        _currentUser.value = null
        if (firebaseUid != null) {
            unsubscribeFromUserTopic(firebaseUid)
        }
    }

    // Topic FCM propre à chaque citoyen : le serveur (une fois disponible) publie
    // dessus pour notifier ce citoyen d'une mise à jour de ses signalements, sans
    // avoir besoin de connaître/stocker un token d'appareil. Best-effort : un échec
    // (pas de réseau, etc.) ne doit pas empêcher la connexion/déconnexion.
    private suspend fun subscribeToUserTopic(firebaseUid: String) {
        runCatching { firebaseMessaging.subscribeToTopic(topicFor(firebaseUid)).await() }
    }

    private suspend fun unsubscribeFromUserTopic(firebaseUid: String) {
        runCatching { firebaseMessaging.unsubscribeFromTopic(topicFor(firebaseUid)).await() }
    }

    private fun topicFor(firebaseUid: String) = "user_$firebaseUid"

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