package com.ibader.citoyenpro.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ibader.citoyenpro.data.local.dao.CategoryDao
import com.ibader.citoyenpro.data.local.dao.IncidentDao
import com.ibader.citoyenpro.data.local.dao.UserDao
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.local.entities.UserEntity
import com.ibader.citoyenpro.domain.model.UserRole
import com.ibader.citoyenpro.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, CategoryEntity::class, IncidentEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun incidentDao(): IncidentDao

    companion object {
        private const val DATABASE_NAME = "citoyenpro.db"

        // Identifiants du compte administrateur de test créé au premier lancement.
        // A retirer (ou changer de mot de passe) avant toute mise en production.
        private const val ADMIN_TEST_EMAIL = "admin@citoyenpro.local"
        private const val ADMIN_TEST_PASSWORD = "admin123"

        private val defaultCategories = listOf(
            CategoryEntity(nom = "Voirie", description = "Chaussées, trottoirs et signalisation routière endommagés"),
            CategoryEntity(nom = "Éclairage public", description = "Lampadaires en panne ou éclairage défectueux"),
            CategoryEntity(nom = "Ordures", description = "Collecte des déchets, dépôts sauvages, poubelles débordantes"),
            CategoryEntity(nom = "Inondation", description = "Accumulation d'eau, canalisations bouchées, égouts débordants"),
            CategoryEntity(nom = "Réseaux", description = "Pannes ou dégâts sur les réseaux d'eau, d'électricité ou de télécommunications"),
            CategoryEntity(nom = "Sécurité", description = "Zones dangereuses, actes de vandalisme, équipements de sécurité défaillants")
        )

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(SeedCallback())
                .build()

        private suspend fun seedDatabase(database: AppDatabase) {
            database.categoryDao().let { categoryDao ->
                defaultCategories.forEach { categoryDao.insert(it) }
            }
            database.userDao().insert(
                UserEntity(
                    nom = "Administrateur",
                    email = ADMIN_TEST_EMAIL,
                    motDePasseHash = PasswordHasher.hash(ADMIN_TEST_PASSWORD),
                    role = UserRole.ADMIN
                )
            )
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                instance?.let { database ->
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }
    }
}
