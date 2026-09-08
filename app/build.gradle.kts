plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Firebase es obligatorio en la Fase 2. El archivo app/google-services.json
// se incluye en esta entrega y el plugin genera automáticamente los recursos
// de configuración de Firebase durante la compilación.
apply(plugin = "com.google.gms.google-services")

android {
    namespace = "com.alertaciudadana.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alertaciudadana.app"
        // minSdk 26 permite usar Adaptive Icons de forma nativa sin recursos
        // raster adicionales y cubre la inmensa mayoría de dispositivos activos.
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        buildConfigField(
            "String",
            "UPDATE_REPOSITORY",
            "\"${System.getenv("GITHUB_REPOSITORY") ?: "TU_USUARIO/InfoVia"}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("SIGNING_KEYSTORE_PATH")
            val storePasswordEnv = System.getenv("SIGNING_STORE_PASSWORD")
            val keyAliasEnv = System.getenv("SIGNING_KEY_ALIAS")
            val keyPasswordEnv = System.getenv("SIGNING_KEY_PASSWORD")
            if (!keystorePath.isNullOrBlank() && !storePasswordEnv.isNullOrBlank() &&
                !keyAliasEnv.isNullOrBlank() && !keyPasswordEnv.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = storePasswordEnv!!
                keyAlias = keyAliasEnv!!
                keyPassword = keyPasswordEnv!!
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            val signingReady = !System.getenv("SIGNING_KEYSTORE_PATH").isNullOrBlank() &&
                !System.getenv("SIGNING_STORE_PASSWORD").isNullOrBlank() &&
                !System.getenv("SIGNING_KEY_ALIAS").isNullOrBlank() &&
                !System.getenv("SIGNING_KEY_PASSWORD").isNullOrBlank()
            if (signingReady) signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // Ubicación (Fused Location Provider) - preparado desde Fase 1
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // OpenStreetMap mediante osmdroid (sin Google Maps SDK).
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Firebase: se usan módulos principales (no KTX) para mantener compatibilidad
    // con las versiones actuales del BoM.
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
