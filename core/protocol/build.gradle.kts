plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    explicitApi()
    // Target attivi: JVM (test, simulatore, server) e Android. iOS si aggiunge nel task F4.
    jvm()
    androidTarget()
    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:game"))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "it.maestro.protocol"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
