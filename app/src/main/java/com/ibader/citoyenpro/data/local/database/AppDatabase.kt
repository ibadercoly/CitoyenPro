package com.ibader.citoyenpro.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ibader.citoyenpro.data.local.dao.CategoryDao
import com.ibader.citoyenpro.data.local.dao.IncidentDao
import com.ibader.citoyenpro.data.local.dao.IncidentStatusHistoryDao
import com.ibader.citoyenpro.data.local.dao.IncidentVoteDao
import com.ibader.citoyenpro.data.local.dao.PendingIncidentOperationDao
import com.ibader.citoyenpro.data.local.dao.UserDao
import com.ibader.citoyenpro.data.local.entities.CategoryEntity
import com.ibader.citoyenpro.data.local.entities.IncidentEntity
import com.ibader.citoyenpro.data.local.entities.IncidentStatusHistoryEntity
import com.ibader.citoyenpro.data.local.entities.IncidentVoteEntity
import com.ibader.citoyenpro.data.local.entities.PendingIncidentOperationEntity
import com.ibader.citoyenpro.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        IncidentEntity::class,
        IncidentStatusHistoryEntity::class,
        PendingIncidentOperationEntity::class,
        IncidentVoteEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun incidentDao(): IncidentDao
    abstract fun incidentStatusHistoryDao(): IncidentStatusHistoryDao
    abstract fun pendingIncidentOperationDao(): PendingIncidentOperationDao
    abstract fun incidentVoteDao(): IncidentVoteDao

    companion object {
        private const val DATABASE_NAME = "citoyenpro.db"

        // Ajout de la table d'historique des changements de statut d'un signalement ;
        // les tables existantes ne sont pas affectées, aucune perte de données.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `incident_status_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `incidentId` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        `commentaire` TEXT,
                        FOREIGN KEY(`incidentId`) REFERENCES `incidents`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_incident_status_history_incidentId` " +
                        "ON `incident_status_history` (`incidentId`)"
                )
            }
        }

        // Ajout de la file d'attente de synchronisation des signalements
        // (IncidentRepository) ; les tables existantes ne sont pas affectées,
        // aucune perte de données.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pending_incident_operations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `incidentId` INTEGER NOT NULL,
                        `operationType` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_pending_incident_operations_incidentId` " +
                        "ON `pending_incident_operations` (`incidentId`)"
                )
            }
        }

        // Ajout du vote citoyen (table + compteur de points sur l'utilisateur) ;
        // les tables existantes ne sont pas affectées, aucune perte de données.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `users` ADD COLUMN `points` INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `incident_votes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `incidentId` INTEGER NOT NULL,
                        `citoyenId` INTEGER NOT NULL,
                        `dateVote` INTEGER NOT NULL,
                        FOREIGN KEY(`incidentId`) REFERENCES `incidents`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`citoyenId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_incident_votes_incidentId_citoyenId` " +
                        "ON `incident_votes` (`incidentId`, `citoyenId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_incident_votes_citoyenId` ON `incident_votes` (`citoyenId`)"
                )
            }
        }

        // Passage à Firebase Authentication : les identifiants (mot de passe
        // haché) ne sont plus stockés localement, remplacés par un firebase_uid
        // qui rattache le profil applicatif (rôle, points) au compte Firebase.
        // Les comptes existants n'ayant pas de compte Firebase correspondant,
        // la table est recréée vide ; chaque utilisateur devra s'inscrire à
        // nouveau (via Firebase) pour obtenir un nouveau profil applicatif.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `users`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `firebase_uid` TEXT NOT NULL,
                        `nom` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `points` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_users_firebase_uid` ON `users` (`firebase_uid`)"
                )
            }
        }

        // Alignement sur l'identité utilisée par le backend : un incident est
        // désormais rattaché à son auteur par son uid Firebase (citoyenUid,
        // String) plutôt qu'un id Room local (citoyenId, Long) — le backend
        // ne connaît pas cet id local et l'app ne connaît pas forcément le
        // profil Room des *autres* citoyens (incidents reçus du serveur), ce
        // qui rendait l'ancienne clé étrangère vers `users` intenable dès
        // qu'un incident d'un autre citoyen était synchronisé. Pas de
        // conversion possible d'un id local vers un uid : la table est
        // recréée vide, ainsi que les tables qui référencent ses lignes
        // (mêmes données de développement, aucune perte en production
        // puisque le backend n'a encore aucun signalement réel).
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM `incident_votes`")
                db.execSQL("DELETE FROM `pending_incident_operations`")
                db.execSQL("DELETE FROM `incident_status_history`")
                db.execSQL("DROP TABLE IF EXISTS `incidents`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `incidents` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `titre` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `priority` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `photo_uri` TEXT,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `adresse` TEXT NOT NULL,
                        `citoyenUid` TEXT NOT NULL,
                        `service_affecte` TEXT,
                        `date_creation` INTEGER NOT NULL,
                        `date_maj` INTEGER NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_incidents_categoryId` ON `incidents` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_incidents_citoyenUid` ON `incidents` (`citoyenUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_incidents_status` ON `incidents` (`status`)")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()

        // Les comptes utilisateurs sont créés via Firebase Authentication
        // (inscription depuis l'app). Les catégories ne sont plus semées ici :
        // elles viennent exclusivement du backend via
        // CategoryRepository.syncFromRemote() (appelé au lancement, cf.
        // AppNavHost), pour éviter un double jeu de catégories (local +
        // serveur) avec des ids différents. Pour créer le premier compte
        // admin : inscrivez-vous normalement depuis l'app, puis passez son
        // rôle à ADMIN en base MySQL.
    }
}
