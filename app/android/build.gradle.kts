plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "it.maestro.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.maestro.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = System.getenv("MAESTRO_KEYSTORE_PATH")
            if (keystoreFile != null && file(keystoreFile).exists()) {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("MAESTRO_KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("MAESTRO_KEY_ALIAS") ?: ""
                keyPassword = System.getenv("MAESTRO_KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    // Schema Room versionato in Git e letto dai test di migrazione (ADR-0017, F1R-03).
    // Robolectric legge gli asset della variante, non quelli del source set di test: lo schema va negli
    // asset di debug, mai in quelli di rilascio.
    sourceSets.getByName("debug").assets.srcDir("$projectDir/schemas")

    testOptions {
        unitTests.isReturnDefaultValues = true
        // Robolectric ha bisogno delle risorse Android per disegnare le schermate.
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            // -Proborazzi.record=true scrive gli screenshot in build/outputs/roborazzi.
            it.systemProperty(
                "roborazzi.test.record",
                project.findProperty("roborazzi.record")?.toString() ?: "false",
            )
            // I test Compose su Robolectric usano l'activity di ui-test-manifest, presente solo in debug:
            // nella variante release girerebbero due volte gli stessi test senza poterli avviare.
            if (it.name.contains("Release")) it.exclude("**/ui/screenshot/**")
            // Lo schema Room per i test di migrazione è solo negli asset di debug (vedi sourceSets sopra).
            if (it.name.contains("Release")) it.exclude("**/data/local/db/MaestroMigrationsTest*")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:game"))
    implementation(project(":core:ai"))
    implementation(project(":core:protocol"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
