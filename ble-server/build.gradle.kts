plugins {
    kotlin("jvm")
}

group = "org.kikermo.bleserver"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.dbus.bom))
    implementation(libs.dbus.core)
    implementation(libs.dbus.transport.jnrunixsockets)

    implementation(project(":bluez-core"))

    implementation(libs.log4j.api)
    implementation(libs.log4j.impl)


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}