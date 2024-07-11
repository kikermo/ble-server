package org.kikermo.bleserver

import org.kikermo.bleserver.org.kikermo.bleserver.BLEAdvertisement
import org.kikermo.bleserver.org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.org.kikermo.bleserver.BLEServer
import org.kikermo.bleserver.org.kikermo.bleserver.BLEService
import java.util.UUID
import kotlin.random.Random

private const val UUID_READ_CHARACTERISTIC = "826c171b-e9d9-423c-a241-665bb0b46bfa"
private const val UUID_WRITE_CHARACTERISTIC = "9371ef59-c4ce-4bea-a33a-1946b2ef2963"
private const val UUID_SERVICE = "215f404b-1413-4b38-90d6-72c183eea77a"

fun main() {
    println("Hello BLE")

    val readCharacteristics = BLECharacteristic(
        uuid = UUID.fromString(UUID_READ_CHARACTERISTIC),
        readAccess = BLECharacteristic.AccessType.Read,
    )
    val writeCharacteristics = BLECharacteristic(
        uuid = UUID.fromString(UUID_WRITE_CHARACTERISTIC),
        readAccess = BLECharacteristic.AccessType.Read,
        writeAccess = BLECharacteristic.AccessType.Write { value ->
            println("New value - $value")
        }
    )
    val service = BLEService(
        uuid = UUID.fromString(UUID_SERVICE),
        characteristics = listOf(writeCharacteristics, readCharacteristics)
    )
    val advertisement = BLEAdvertisement()
    val server = BLEServer(
        services = listOf(service),
        advertisement = advertisement
    ) {
        println("Connection change")
    }

    server.start()

    while (true) {
        try {
            Thread.sleep(5000) // 5 seconds

            println("Service is running...")
            readCharacteristics.value = Random.nextBytes(2)
        } catch (e: InterruptedException) {

            println("Service was interrupted.")
            server.stop()
        }
    }
}
