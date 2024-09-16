package org.kikermo.bleserver.sample

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.bluez.BluezBLEServerConnector
import org.kikermo.bleserver.dsl.bleServer
import java.time.Duration
import kotlin.random.Random

private const val UUID_READ_CHARACTERISTIC = "826c171b-e9d9-423c-a241-665bb0b46bfa"
private const val UUID_WRITE_CHARACTERISTIC = "9371ef59-c4ce-4bea-a33a-1946b2ef2963"
private const val UUID_PRIMARY_SERVICE = "215f404b-1413-4b38-90d6-72c183eea77a"
//private const val UUID_SECONDARY_SERVICE = "56dc46db-5795-48c1-bc6d-4bfb0310433b"
private const val SERVER_NAME = "sampleble"
private const val SERVICE_NAME = "sampleservice"
//private const val SERVICE_NAME_SECONDARY = "samblesecondary"

private fun characteristicFlow() = flow {
    delay(Duration.ofSeconds(1))
    while (true) {
        emit(Random.nextBytes(2))
        delay(Duration.ofSeconds(4))
    }
}

fun runSimpleServerDSL() {
    println("DSL Sample")


    val server = bleServer {
        serverName = SERVER_NAME
        bleServerConnector = BluezBLEServerConnector()

        connectionListener {
            onDeviceConnected = { deviceName, deviceAddress ->
                println("device connected $deviceName, $deviceAddress")
            }
            onDeviceDisconnected = {
                println("device disconnected")
            }
        }

        primaryService {
            uuid = UUID_PRIMARY_SERVICE.toUUID()
            name = SERVICE_NAME

            characteristic {
                uuid = UUID_READ_CHARACTERISTIC.toUUID()

                readAccess = BLECharacteristic.AccessType.Read
                notifyAccess = BLECharacteristic.AccessType.Notify
                name = "heartbeat"
                initialValue = Random.nextBytes(2)

                valueChangingAction { valueSetter ->
                    runBlocking {
                        characteristicFlow().collect(valueSetter::invoke)
                    }
                }
            }
            characteristic {
                uuid = UUID_WRITE_CHARACTERISTIC.toUUID()
                readAccess = BLECharacteristic.AccessType.Read
                writeAccess = BLECharacteristic.AccessType.Write { value ->
                    println("New value - $value")
                }
                name = "temperature"
                initialValue = Random.nextBytes(2)

            }
        }
//        service {
//            uuid = UUID_SECONDARY_SERVICE.toUUID()
//            name = SERVICE_NAME_SECONDARY
//
//            characteristic {
//                uuid = UUID.randomUUID()
//                name = "moisture"
//
//                readAccess = BLECharacteristic.AccessType.Read
//            }
//        }
    }


    while (true) {
        try {
            Thread.sleep(5000) // 5 seconds

            println("Service is running...")
        } catch (e: InterruptedException) {

            println("Service was interrupted.")
            server.stop()
        }
    }
}
