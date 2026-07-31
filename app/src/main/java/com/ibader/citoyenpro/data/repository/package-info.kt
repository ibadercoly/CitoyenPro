/**
 * Repositories : point d'entrée unique utilisé par les ViewModels pour
 * accéder aux données. Orchestrent [com.ibader.citoyenpro.data.local] (cache/offline)
 * et [com.ibader.citoyenpro.data.remote] (API), exposent des modèles du domaine
 * et masquent l'origine des données au reste de l'application.
 */
package com.ibader.citoyenpro.data.repository