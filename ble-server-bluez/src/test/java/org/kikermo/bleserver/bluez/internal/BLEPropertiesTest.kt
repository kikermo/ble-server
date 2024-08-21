package org.kikermo.bleserver.bluez.internal

import org.freedesktop.dbus.DBusPath
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.BLEService
import org.kikermo.bleserver.internal.toProperties
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BLEPropertiesTest {
    private val characteristicUUID = UUID.randomUUID()
    private val characteristicName = "sampleCharacteristic"

    private val servicePath = "/sample/sampleService"
    private val serviceUUID = UUID.randomUUID()
    private val serviceName = "sampleService"

    private val serverName = "sampleServer"

    @Test
    @DisplayName("map characteristic")
    fun mapCharacteristic() {
        // given
        val bleCharacteristic = BLECharacteristic(
            uuid = characteristicUUID,
            name = characteristicName,
        )

        // when
        val result = bleCharacteristic.toProperties(servicePath)

        // then
        assertNotNull(result["org.bluez.GattCharacteristic1"])
        assertEquals(characteristicUUID.toString(), result["org.bluez.GattCharacteristic1"]?.get("UUID")?.value)
        assertNotNull(result["org.bluez.GattCharacteristic1"]?.get("Service"))
        val serviceDBusPath = result["org.bluez.GattCharacteristic1"]?.get("Service")?.value
        assertEquals(DBusPath(servicePath), serviceDBusPath)
    }

    @Test
    @DisplayName("map no characteristic flags")
    fun mapCharacteristicNoFlags() {
        // given
        val bleCharacteristic = BLECharacteristic(
            uuid = characteristicUUID,
            name = characteristicName,
        )

        // when
        val result = bleCharacteristic.toProperties(servicePath)
        val flags = result["org.bluez.GattCharacteristic1"]?.get("Flags")?.value as Array<String>

        // then
        assert(flags.isEmpty())
    }

    @Test
    @DisplayName("map characteristic flags")
    fun mapAllCharacteristicFlags() {
        // given
        val bleCharacteristic = BLECharacteristic(
            uuid = characteristicUUID,
            name = characteristicName,
            readAccess = BLECharacteristic.AccessType.Read,
            writeAccess = BLECharacteristic.AccessType.Write {},
            notifyAccess = BLECharacteristic.AccessType.Notify
        )

        // when
        val result = bleCharacteristic.toProperties(servicePath)
        val flags = result["org.bluez.GattCharacteristic1"]?.get("Flags")?.value as Array<String>

        // then
        assert(flags.contains("read"))
        assert(flags.contains("write"))
        assert(flags.contains("notify"))
    }

    @Test
    @DisplayName("map service")
    fun mapService() {
        // given
        val bleService = BLEService(
            uuid = serviceUUID,
            name = serviceName,
            characteristics = listOf()
        )

        // when
        val result = bleService.toProperties(true, serviceName)

        // then
        assertNotNull(result["org.bluez.GattService1"])
        assertEquals(serviceUUID.toString(), result["org.bluez.GattService1"]?.get("UUID")?.value)
        assertEquals(true,result["org.bluez.GattService1"]?.get("Primary")?.value)
        assertNotNull(result["org.bluez.GattService1"]?.get("Characteristics"))
    }
}