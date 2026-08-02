# CitoyenPro

Application Android citoyenne permettant à un habitant de signaler un incident du
quotidien (voirie, éclairage, ordures, inondation, réseaux, sécurité...) à sa
municipalité, de suivre son traitement, et à une équipe d'administrateurs de le
qualifier, l'assigner à un service et le résoudre.

Deux espaces distincts cohabitent dans la même application :

- **Espace citoyen** : signaler, suivre et soutenir des signalements, gagner des
  points et des badges.
- **Espace administrateur** : tableau de bord, gestion des signalements, des
  catégories et des utilisateurs.

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Technologies utilisées](#technologies-utilisées)
- [Prérequis](#prérequis)
- [Installation et lancement](#installation-et-lancement)
- [Comptes de test](#comptes-de-test)
- [Structure du projet](#structure-du-projet)

## Fonctionnalités

### Deux fonctionnalités innovantes

- **Synchronisation hors-ligne intelligente.** Un citoyen peut créer, consulter
  et modifier ses signalements sans connexion réseau : chaque opération est
  d'abord persistée localement (Room) puis empilée dans une file d'attente
  (`pending_incident_operations`). Dès que le réseau revient, la file est
  automatiquement rejouée vers l'API distante (`ConnectivitySyncTrigger`), avec
  un filet de sécurité périodique en tâche de fond (`IncidentSyncWorker`,
  WorkManager) qui rattrape les cas manqués. Aucune action manuelle de
  synchronisation n'est nécessaire.
- **Gamification civique et vote communautaire.** Chaque signalement créé et
  chaque changement de statut rapporte des points au citoyen, qui débloque
  progressivement des badges (Bronze, Argent, Or, Platine). Les autres citoyens
  peuvent en parallèle soutenir (voter pour) les signalements publics les plus
  importants, faisant remonter collectivement les urgences du quartier.

### Côté citoyen

- Inscription / connexion sécurisée (Firebase Authentication)
- Création d'un signalement : titre, description, catégorie, priorité, photo
  (prise directe ou galerie), géolocalisation (position actuelle)
- Suivi de ses signalements avec historique détaillé des changements de statut
- Fil public des signalements de la ville, avec vote/soutien communautaire
- Points et badges de citoyen engagé
- Notifications de changement de statut : locales sur l'appareil qui effectue
  l'action, et push distantes via Firebase Cloud Messaging (abonnement à un
  topic propre à chaque citoyen) pour les autres appareils
- Fonctionnement hors-ligne avec synchronisation automatique
- Profil citoyen (identité, rôle, nombre de signalements)

### Côté administrateur

- Tableau de bord avec compteurs par statut
- Liste des signalements avec filtres (statut, priorité, catégorie)
- Détail d'un signalement : changement de statut, affectation à un service,
  historique complet
- Gestion des catégories (création, modification, suppression protégée si des
  signalements y sont encore rattachés)
- Statistiques (répartition des signalements par statut et par catégorie)
- Liste des comptes utilisateurs

## Architecture

L'application suit une architecture **MVVM** (Model - View - ViewModel), sans
framework d'injection de dépendances : les repositories et services sont
construits une seule fois dans `MainActivity.onCreate()` puis transmis en
paramètres à travers l'arbre Compose.

- **UI (`ui/`)** : un écran = un trio `Screen` (Composable, sans logique
  métier) + `UiState` (état immuable affiché) + `ViewModel` (expose l'état en
  `StateFlow`, réagit aux événements utilisateur). Regroupés par espace :
  `ui/auth`, `ui/citizen`, `ui/admin`, plus des composants partagés dans
  `ui/common` (badge de statut, compteur statistique, indicateur de synchro) et
  la navigation dans `ui/navigation`.
- **Domaine (`domain/model`)** : modèles et règles métier indépendants de toute
  UI ou source de données (statuts de signalement, rôles, priorités, règles de
  points/badges).
- **Données (`data/`)** :
  - `data/local` : Room (entités, DAO, base et migrations).
  - `data/remote` : client Retrofit/OkHttp, DTO et mappers vers les entités.
  - `data/repository` : point d'entrée unique utilisé par les ViewModels,
    combine Room, Retrofit et Firebase Authentication.
  - `data/sync` : synchronisation hors-ligne en tâche de fond (WorkManager).
- **`notification/`** : notifications locales de changement de statut et
  service Firebase Cloud Messaging pour les push distantes.

## Technologies utilisées

| Domaine | Technologie |
|---|---|
| Langage | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01), Material 3 |
| Navigation | Navigation Compose 2.9.7 |
| Cycle de vie | Lifecycle / ViewModel 2.11.0 |
| Asynchrone | Coroutines & Flow 1.11.0 |
| Persistance locale | Room 2.8.4 (+ KSP pour la génération de code) |
| Réseau | Retrofit 3.0.0, OkHttp 4.12.0, Gson |
| Authentification | Firebase Authentication (BOM 34.16.0) |
| Notifications push | Firebase Cloud Messaging |
| Tâches de fond | WorkManager 2.10.0 |
| Cartographie | osmdroid 6.1.20 (tuiles OpenStreetMap) |
| Images | Coil 3.2.0 |
| Permissions runtime | Accompanist Permissions 0.37.3 |
| Géolocalisation | Play Services Location 21.3.0 |
| Build | Android Gradle Plugin 9.3.1, KSP |

Configuration Android : `compileSdk`/`targetSdk` 37, `minSdk` 24, Java 11.

## Prérequis

- [Android Studio](https://developer.android.com/studio) récent (compatible
  AGP 9.3.1 / Kotlin 2.2.10) avec le SDK Android 37 installé
- JDK 11 ou supérieur
- Un projet [Firebase](https://console.firebase.google.com/) avec
  **Authentication** (fournisseur Email/Mot de passe) et **Cloud Messaging**
  activés

## Installation et lancement

1. **Cloner le dépôt**

   ```bash
   git clone <url-du-depot>
   cd CitoyenPro
   ```

2. **Configurer Firebase**

   Dans la [console Firebase](https://console.firebase.google.com/), créer (ou
   réutiliser) un projet Android avec l'ID d'application
   `com.ibader.citoyenpro`, activer Authentication (Email/Mot de passe) et
   Cloud Messaging, puis télécharger le fichier `google-services.json` et le
   placer à la racine du module app :

   ```
   app/google-services.json
   ```

   Ce fichier n'est pas fourni dans le dépôt : chaque environnement (dev,
   CI...) doit y placer le sien.

3. **Ouvrir le projet**

   Ouvrir le dossier `CitoyenPro` dans Android Studio et laisser la
   synchronisation Gradle se terminer (elle télécharge les dépendances listées
   dans `gradle/libs.versions.toml`).

4. **(Optionnel) Backend REST**

   Le backend REST utilisé pour la synchronisation des signalements n'est pas
   encore déployé : l'URL de développement (`RetrofitClient.kt`) pointe vers
   `http://10.0.2.2:8080/api/`, c'est-à-dire un serveur local sur la machine
   hôte vu depuis l'émulateur Android. L'authentification, la création de
   signalements et leur consultation locale fonctionnent sans lui ; seule la
   synchronisation distante échouera et sera automatiquement retentée plus
   tard (cf. mode hors-ligne).

5. **Lancer l'application**

   Depuis Android Studio : sélectionner un émulateur ou un appareil physique
   connecté, puis lancer la configuration `app` (Run ▶). En ligne de commande :

   ```bash
   ./gradlew installDebug
   ```

## Comptes de test

Aucun compte n'est préchargé dans l'application : au premier lancement, seules
les catégories de signalement par défaut (Voirie, Éclairage public, Ordures,
Inondation, Réseaux, Sécurité) sont créées.

- **Compte citoyen** : utiliser l'écran « Inscription » de l'application (nom,
  email, mot de passe). Un compte Firebase est créé et un profil applicatif
  local lui est automatiquement rattaché avec le rôle `CITOYEN`.
- **Compte administrateur** : il n'existe pas d'écran de promotion dans l'app.
  Pour en créer un en environnement de développement :
  1. S'inscrire normalement comme un citoyen depuis l'application.
  2. Dans Android Studio, ouvrir **View ▸ Tool Windows ▸ App Inspection ▸
     Database Inspector** pendant que l'app tourne, sélectionner la base
     `citoyenpro.db`, ouvrir la table `users` et changer la valeur de la
     colonne `role` de `CITOYEN` à `ADMIN` pour le compte concerné.
  3. Se déconnecter puis se reconnecter dans l'app pour que le nouveau rôle
     soit pris en compte.

## Structure du projet

```
app/src/main/java/com/ibader/citoyenpro/
├── data/
│   ├── local/         # Room : entités, DAO, base et migrations
│   ├── remote/         # Retrofit, DTO, mappers
│   ├── repository/      # Source de vérité unique par domaine métier
│   └── sync/           # Synchronisation hors-ligne (WorkManager)
├── domain/
│   └── model/           # Modèles et règles métier (statuts, rôles, badges...)
├── notification/         # Notifications locales + Firebase Cloud Messaging
├── ui/
│   ├── auth/            # Connexion / inscription
│   ├── citizen/          # Espace citoyen
│   ├── admin/           # Espace administrateur
│   ├── common/           # Composants partagés
│   ├── navigation/        # Graphe de navigation
│   └── theme/            # Design system (couleurs, formes, typographie)
└── util/                # Utilitaires transverses (réseau, permissions...)
```
