package org.kikermo.bleserver.internal

import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.types.Variant
import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.BLEService

internal const val GATT_CHARACTERISTIC_INTERFACE = "org.bluez.GattCharacteristic1"
private const val GATT_SERVICE_INTERFACE = "org.bluez.GattService1"
private const val LEADVERTISEMENT_INTERFACE = "org.bluez.LEAdvertisement1"

private const val SERVICE_UUID_PROPERTY_KEY = "UUID";
private const val SERVICE_PRIMARY_PROPERTY_KEY = "Primary";
private const val SERVICE_CHARACTERISTIC_PROPERTY_KEY = "Characteristics";

private const val CHARACTERISTIC_SERVICE_PROPERTY_KEY = "Service"
private const val CHARACTERISTIC_UUID_PROPERTY_KEY = "UUID"
private const val CHARACTERISTIC_FLAGS_PROPERTY_KEY = "Flags"
private const val CHARACTERISTIC_DESCRIPTORS_PROPERTY_KEY = "Descriptors"

private const val CHARACTERISTIC_FLAG_READ = "read"
private const val CHARACTERISTIC_FLAG_WRITE = "write"
private const val CHARACTERISTIC_FLAG_NOTIFY = "notify"

private const val PATH_DBUS_DIVIDER = "/"

private const val ADVERTISEMENT_TYPE_PERIPHERAL = "peripheral"
private const val ADVERTISEMENT_TYPE_PROPERTY_KEY = "Type"
private const val ADVERTISEMENT_SERVICES_UUIDS_PROPERTY_KEY = "ServiceUUIDs"
private const val ADVERTISEMENT_LOCAL_NAME_PROPERTY_KEY = "LocalName"
private const val ADVERTISEMENT_INCLUDE_TX_POWER_PROPERTY_KEY = "IncludeTxPower"

internal fun BLECharacteristic.toProperties(
    serverPath: String,
): Map<String, Map<String, Variant<*>>> {
    println("Characteristic -> getCharacteristicProperties")

    val characteristicProperties = buildMap {
        put(CHARACTERISTIC_SERVICE_PROPERTY_KEY, Variant(serverPath))
        put(CHARACTERISTIC_UUID_PROPERTY_KEY, Variant(uuid))
        put(CHARACTERISTIC_FLAGS_PROPERTY_KEY, Variant(toFlags()))
        put(CHARACTERISTIC_DESCRIPTORS_PROPERTY_KEY, Variant(arrayOf<DBusPath>()))
    }

    return mapOf(GATT_CHARACTERISTIC_INTERFACE to characteristicProperties)
}

internal fun BLEService.toProperties(isPrimary: Boolean, serverName: String): Map<String, Map<String, Variant<*>>> {
    println("Service -> getServiceProperties")

    val serviceMap = buildMap {
        put(SERVICE_UUID_PROPERTY_KEY, Variant(uuid))
        put(SERVICE_PRIMARY_PROPERTY_KEY, Variant(isPrimary))
        val characteristicPaths = characteristics.map { characteristic ->
            characteristic.toPath(toPath(serverName).path)
        }.toTypedArray()
        put(SERVICE_CHARACTERISTIC_PROPERTY_KEY, Variant(characteristicPaths))
    }

    return mapOf(GATT_SERVICE_INTERFACE to serviceMap)
}

internal fun toAdvertisementProperties(
    bleServices: List<BLEService>,
    serverName: String
) = buildMap {
    val advertisementProperties = buildMap<String, Variant<*>> {
        put(ADVERTISEMENT_TYPE_PROPERTY_KEY, Variant(ADVERTISEMENT_TYPE_PERIPHERAL))

        val serviceUUIDs = bleServices.map {
            it.uuid.toString()
        }
        put(ADVERTISEMENT_SERVICES_UUIDS_PROPERTY_KEY, Variant(serviceUUIDs.toTypedArray()))
        put(ADVERTISEMENT_LOCAL_NAME_PROPERTY_KEY, Variant(serverName))
        put(ADVERTISEMENT_INCLUDE_TX_POWER_PROPERTY_KEY, Variant(true))
    }.toMutableMap()
    put(LEADVERTISEMENT_INTERFACE, advertisementProperties)
}.toMutableMap()

private fun BLECharacteristic.toFlags(): Array<String> {
    val flags: List<String> = buildList {
        if (readAccess != null) {
            add(CHARACTERISTIC_FLAG_READ)
        }
        if (writeAccess != null) {
            add(CHARACTERISTIC_FLAG_WRITE)
        }
        if (notifyAccess != null) {
            add(CHARACTERISTIC_FLAG_NOTIFY)
        }
    }

    return flags.toTypedArray()
}

internal fun BLEService.toPath(serverName: String): DBusPath {
    return DBusPath("$PATH_DBUS_DIVIDER$serverName$PATH_DBUS_DIVIDER$name")
}

internal fun BLECharacteristic.toPath(servicePath: String): String {
    return "$servicePath$PATH_DBUS_DIVIDER$name"
}