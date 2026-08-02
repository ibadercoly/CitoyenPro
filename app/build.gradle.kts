plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.ibader.citoyenpro"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ibader.citoyenpro"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Backend de dev joint via USB (adb reverse tcp:3000 tcp:3000) plutôt
        // que par IP Wi-Fi : le Wi-Fi local isole les appareils entre eux sur
        // les réseaux testés jusqu'ici (connexion refusée/impossible entre
        // téléphone et PC malgré même sous-réseau et pare-feu correctement
        // configuré). Nécessite de relancer `adb reverse tcp:3000 tcp:3000`
        // à chaque nouvelle session de débogage (la redirection ne survit pas
        // à un débranchement/reboot). Seul endroit où l'URL est définie, lue
        // via BuildConfig.API_BASE_URL (cf. RetrofitClient) plutôt que
        // dupliquée en dur.
        buildConfigField("String", "API_BASE_URL", "\"http://127.0.0.1:3000/\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Interface (Compose + Material 3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)

    // Cycle de vie + ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Room (persistance locale)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coil (chargement d'images)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Retrofit + Gson + logging OkHttp (accès réseau)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // osmdroid (cartographie OpenStreetMap)
    implementation(libs.osmdroid.android)

    // Permissions runtime en Compose
    implementation(libs.accompanist.permissions)

    // Géolocalisation (FusedLocationProviderClient)
    implementation(libs.play.services.location)

    // WorkManager (synchronisation en arrière-plan)
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase Authentication + Cloud Messaging (BOM aligne les versions de tous les artefacts Firebase)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

    // Pont coroutines <-> Task (Firebase) pour utiliser .await()
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}