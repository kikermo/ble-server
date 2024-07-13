plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "BLE-Server"
include("ble-server")
include("ble-server-sample")
include("bluez-core")
