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
            implementation(project(":core:game"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
