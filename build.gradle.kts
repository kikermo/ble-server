plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.kikermo.bleserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}