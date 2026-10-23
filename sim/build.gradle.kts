plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:game"))
    implementation(project(":core:ai"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("it.maestro.sim.MainKt")
}
