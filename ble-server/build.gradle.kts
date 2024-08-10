plugins {
    kotlin("jvm")
    `maven-publish`
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
//    implementation(libs.dbus.transport.junixsocket)
//    implementation(libs.dbus.transport.nativeunixsockets)

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.kikermo.bleserver"
            artifactId = "core"
            version = libs.versions.bleserver.get()

            from(components["java"])
        }
    }
}