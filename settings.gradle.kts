// Elenco dei moduli. Aggiungere un modulo richiede un ADR (docs/decisions).
rootProject.name = "maestro"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

include(":core:game")
include(":core:ai")
include(":core:protocol")
include(":sim")
include(":app:android")

// Fase 2: include(":server") — non creare prima del task F2 dedicato.
