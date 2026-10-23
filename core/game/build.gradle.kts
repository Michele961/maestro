plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    explicitApi()
    // Target attivi: JVM (test, simulatore, server). Android e iOS si aggiungono nei task F1/F4.
    jvm()
    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
            // Nessuna dipendenza: solo stdlib Kotlin (ADR-0010).
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
