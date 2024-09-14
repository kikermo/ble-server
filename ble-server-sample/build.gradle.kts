plugins {
    kotlin("jvm")
    application
}

dependencies {
//    implementation(libs.bleserver.core)
//    implementation(libs.bleserver.bluez)
    implementation(project(":ble-server"))
    implementation(project(":ble-server-bluez"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

}

repositories {
    mavenCentral()
    mavenLocal()
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
