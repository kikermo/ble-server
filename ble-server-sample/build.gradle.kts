plugins {
    kotlin("jvm")
}

group = "org.kikermo.bleserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ble-server"))
}

kotlin {
    jvmToolchain(11)
}
