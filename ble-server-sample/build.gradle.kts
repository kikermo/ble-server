plugins {
    kotlin("jvm")
    application
}

group = "org.kikermo.bleserver"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ble-server"))
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