plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(libs.bleserver.core)
    implementation(libs.bleserver.bluez)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.kikermo.bleserver.MainKt") // Replace with your main class
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Main-Class" to "org.kikermo.bleserver.MainKt"
            ),
        )
    }
}