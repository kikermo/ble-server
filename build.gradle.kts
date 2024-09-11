plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.kikermo.bleserver"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

allprojects {
    group = "org.kikermo.bleserver"
    version = "0.0.1"
}

object Meta {
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}
