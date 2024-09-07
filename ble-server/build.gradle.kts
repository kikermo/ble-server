plugins {
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
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

            from(components["kotlin"])
        }
    }
}