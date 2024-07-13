plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.dbus.bom))
    implementation(libs.dbus.core)
}