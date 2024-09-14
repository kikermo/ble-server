package org.kikermo.bleserver

import org.kikermo.bleserver.bluez.BluezBLEServerConnector
import org.kikermo.bleserver.dsl.bleService
import java.util.UUID
import kotlin.random.Random

private const val UUID_READ_CHARACTERISTIC = "826c171b-e9d9-423c-a241-665bb0b46bfa"
private const val UUID_WRITE_CHARACTERISTIC = "9371ef59-c4ce-4bea-a33a-1946b2ef2963"
private const val UUID_SERVICE = "215f404b-1413-4b38-90d6-72c183eea77a"
private const val SERVER_NAME = "sampleble"
private const val SERVICE_NAME = "sampleservice"

private fun String.toUUID() = UUID.fromString(this)

fun main() {
    println("Hello BLE")

    val readCharacteristics = BLECharacteristic(
        uuid = UUID.fromString(UUID_READ_CHARACTERISTIC),
        readAccess = BLECharacteristic.AccessType.Read,
        notifyAccess = BLECharacteristic.AccessType.Notify,
        name = "heartbeat",
    )
    readCharacteristics.value = byteArrayOf(1, 2, 3)
    val writeCharacteristics = BLECharacteristic(
        uuid = UUID.fromString(UUID_WRITE_CHARACTERISTIC),
        readAccess = BLECharacteristic.AccessType.Read,
        writeAccess = BLECharacteristic.AccessType.Write { value ->
            println("New value - $value")
        },
        name = "temperature"
    )
//    val service = BLEService(
//        uuid = UUID.fromString(UUID_SERVICE),
//        name = SERVICE_NAME,
//        characteristics = listOf(writeCharacteristics, readCharacteristics)
//    )
//    val service = BLEService.Builder()
//        .uuid(UUID.fromString(UUID_SERVICE))
//        .name(SERVICE_NAME)
//        .characteristics(listOf(writeCharacteristics,readCharacteristics))
//        .build()

    val service = bleService {
        uuid = UUID_SERVICE.toUUID()
        name = SERVICE_NAME

        characteristics {
            characteristic {
                uuid = UUID_READ_CHARACTERISTIC.toUUID()

                readAccess = BLECharacteristic.AccessType.Read
                notifyAccess = BLECharacteristic.AccessType.Notify
                name = "heartbeat"
            }
            characteristic {
                uuid = UUID_WRITE_CHARACTERISTIC.toUUID()
                readAccess = BLECharacteristic.AccessType.Read
                writeAccess = BLECharacteristic.AccessType.Write { value ->
                    println("New value - $value")
                }
                name = "temperature"
            }
        }
    }

    val connectionListener = object : BLEConnectionListener {
        override fun onDeviceConnected(deviceName: String, deviceAddress: String) {
            println("device connected $deviceName, $deviceAddress")
        }

        override fun onDeviceDisconnected() {
            println("device disconnected")
        }

    }
    val server = BLEServer(
        services = listOf(service),
        serverName = SERVER_NAME,
        connectionListener = connectionListener,
        bleServerConnector = BluezBLEServerConnector()
    )
    server.primaryService = service


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
