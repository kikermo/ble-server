plugins {
    id("java")
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":ble-server"))

    implementation(platform(libs.dbus.bom))
    implementation(libs.dbus.core)
    implementation(libs.dbus.transport.jnrunixsockets)
//    implementation(libs.dbus.transport.junixsocket)
//    implementation(libs.dbus.transport.nativeunixsockets)

    implementation(libs.log4j.api)
    implementation(libs.log4j.impl)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.kikermo.bleserver"
            artifactId = "bluez"
            version = libs.versions.bleserver.get()

            from(components["java"])
        }
    }
}