package com.ibader.citoyenpro.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Crée un fichier vide dans le stockage interne persistant de l'app (filesDir,
// pas cacheDir : le système peut purger le cache sous pression de stockage, ce
// qui perdrait les photos jointes aux signalements déjà enregistrés) et
// retourne son URI de contenu via FileProvider, destinée à recevoir la photo
// prise par l'appareil photo.
fun createImageCaptureUri(context: Context): Uri {
    val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "incident_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
