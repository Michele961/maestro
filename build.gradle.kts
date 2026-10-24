plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
}

/*
 * Confini tra moduli (ADR-0001, ADR-0010).
 * Un modulo può dipendere solo dai progetti e dalle librerie elencati qui.
 * Modificare questa tabella richiede un ADR approvato.
 */
val allowedProjectDeps: Map<String, Set<String>> = mapOf(
    ":core:game" to emptySet(),
    ":core:ai" to setOf(":core:game"),
    ":core:protocol" to setOf(":core:game"),
    ":sim" to setOf(":core:game", ":core:ai"),
    ":app:android" to setOf(":core:game", ":core:ai", ":core:protocol"),
)
val allowedExternalDeps: Map<String, Set<String>> = mapOf(
    ":core:game" to emptySet(),
    ":core:ai" to emptySet(),
    ":core:protocol" to setOf("org.jetbrains.kotlinx:kotlinx-serialization-json"),
    ":sim" to emptySet(),
    ":app:android" to setOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android",
        "androidx.compose:compose-bom",
        "androidx.compose.ui:ui",
        "androidx.compose.ui:ui-graphics",
        "androidx.compose.ui:ui-tooling",
        "androidx.compose.ui:ui-tooling-preview",
        "androidx.compose.ui:ui-test-junit4",
        "androidx.compose.ui:ui-test-manifest",
        "androidx.compose.material3:material3",
        "androidx.activity:activity-compose",
        "androidx.navigation:navigation-compose",
        "androidx.lifecycle:lifecycle-runtime-compose",
        "androidx.lifecycle:lifecycle-viewmodel-compose",
        "io.insert-koin:koin-android",
        "io.insert-koin:koin-androidx-compose",
        "androidx.room:room-runtime",
        "androidx.room:room-ktx",
        "androidx.room:room-compiler",
        "androidx.datastore:datastore-preferences",
        "androidx.datastore:datastore-preferences-core",
        // Solo test: screenshot della UI (F1-18, approvato da Michele)
        "junit:junit",
        "org.robolectric:robolectric",
        "io.github.takahirom.roborazzi:roborazzi",
        "io.github.takahirom.roborazzi:roborazzi-compose",
    ),
)

// Unica eccezione sempre ammessa: la stdlib Kotlin. Nei test è ammesso solo kotlin-test.
fun isStdlib(group: String?, name: String) = group == "org.jetbrains.kotlin" && name.startsWith("kotlin-stdlib")
fun isKotlinTest(group: String?, name: String) = group == "org.jetbrains.kotlin" && name.startsWith("kotlin-test")
val checkedSuffixes = listOf("Implementation", "Api", "CompileOnly", "RuntimeOnly")
val checkedPlain = setOf("implementation", "api", "compileOnly", "runtimeOnly")

// Ogni modulo registrato riceve il controllo; un modulo non registrato fa fallire la build.
subprojects {
    val modulePath = path
    if (childProjects.isNotEmpty() && modulePath !in allowedProjectDeps) return@subprojects // es. ":core", contenitore
    val checkBoundaries = tasks.register("checkModuleBoundaries") {
        group = "verification"
        description = "Verifica che il modulo rispetti i confini definiti nel build root."
        doLast {
            val allowedProjects = allowedProjectDeps[modulePath]
                ?: error("Modulo $modulePath non registrato in allowedProjectDeps (serve un ADR).")
            val allowedExternal = allowedExternalDeps.getValue(modulePath)
            val violations = mutableListOf<String>()
            configurations
                .filter { c -> c.name in checkedPlain || checkedSuffixes.any { c.name.endsWith(it) } }
                .forEach { c ->
                    val isTest = c.name.contains("test", ignoreCase = true)
                    c.dependencies.forEach { d ->
                        when (d) {
                            is ProjectDependency -> if (d.path !in allowedProjects) {
                                violations += "${c.name}: progetto ${d.path}"
                            }
                            else -> {
                                // Il plugin KMP aggiunge dipendenze interne "unspecified"
                                // (group null) alle configurazioni di compilazione dei test.
                                // Non sono dipendenze reali del modulo e vanno ignorate.
                                if (d.group == null || d.name == "unspecified") return@forEach
                                val ok = isStdlib(d.group, d.name) ||
                                    (isTest && isKotlinTest(d.group, d.name)) ||
                                    "${d.group}:${d.name}" in allowedExternal
                                if (!ok) violations += "${c.name}: libreria ${d.group}:${d.name}"
                            }
                        }
                    }
                }
            if (violations.isNotEmpty()) {
                throw GradleException(
                    "Dipendenze non ammesse in $modulePath (vedi docs/architecture.md):\n" +
                        violations.joinToString("\n") { "  - $it" },
                )
            }
        }
    }
    tasks.matching { it.name == "check" }.configureEach { dependsOn(checkBoundaries) }

    tasks.withType<Test>().configureEach {
        testLogging {
            events("failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
