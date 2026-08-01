package com.ibader.citoyenpro.domain.model

// Paliers de badges dérivés du total de points du citoyen : purement calculés
// (aucune table dédiée), pour qu'un changement de seuil n'exige jamais de
// migration ni de rattrapage de données.
enum class Badge(val libelle: String, val seuil: Int) {
    BRONZE("Citoyen bronze", 20),
    ARGENT("Citoyen argent", 50),
    OR("Citoyen or", 100),
    PLATINE("Citoyen platine", 250)
}

fun badgesUnlocked(points: Int): List<Badge> = Badge.entries.filter { points >= it.seuil }

fun nextBadge(points: Int): Badge? = Badge.entries.firstOrNull { points < it.seuil }

// Règles d'attribution de points : un signalement créé fait progresser le
// citoyen une fois, chaque changement de statut ultérieur (généralement
// déclenché par un admin) récompense le suivi du dossier jusqu'à sa
// résolution.
object CitizenPointsRules {
    const val NOUVEAU_SIGNALEMENT = 10
    const val CHANGEMENT_STATUT = 5
}
