plugins {
    kotlin("jvm")
}

group = "org.kikermo.bleserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.dbus.bom))
    implementation(libs.dbus.core)
    implementation(libs.dbus.transport.jnrunixsockets)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}