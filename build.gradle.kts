plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

allprojects {
    group = "org.kikermo.bleserver"
    version = (properties["version"] as String?) ?: "0.0.1-SNAPSHOT"
}
