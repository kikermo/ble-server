package org.kikermo.bleserver.internal

import org.bluez.GattApplication1
import org.bluez.GattManager1
import org.bluez.LEAdvertisement1
import org.bluez.LEAdvertisingManager1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.interfaces.ObjectManager
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.BLEService

internal class BLEServerConnector {
    companion object {
        private const val BLUEZ_DBUS_BUS_NAME = "org.bluez"
        private const val BLUEZ_GATT_INTERFACE = "org.bluez.GattManager1"
        private const val BLUEZ_LE_ADV_INTERFACE = "org.bluez.LEAdvertisingManager1"
        private const val BLUEZ_ADAPTER_INTERFACE = "org.bluez.Adapter1"

        private const val ADVERTISEMENT_TYPE_PERIPHERAL = "peripheral"
        private const val ADVERTISEMENT_TYPE_PROPERTY_KEY = "Type"
        private const val ADVERTISEMENT_SERVICES_UUIDS_PROPERTY_KEY = "ServiceUUIDs"
        private const val ADVERTISEMENT_LOCAL_NAME_PROPERTY_KEY = "LocalName"
        private const val ADVERTISEMENT_INCLUDE_TX_POWER_PROPERTY_KEY = "IncludeTxPower"


        private const val PATH_DBUS_ROOT = "/"
        private const val PATH_ADVERTISEMENT_SUFFIX = "/advertisement"
        private const val PATH_SERVICE_SUFFIX = "/s"
        private const val PATH_CHARACTERISTIC_SUFFIX = "/c"

        private const val LEADVERTISEMENT_INTERFACE = "org.bluez.LEAdvertisement1"
        private const val GATT_CHARACTERISTIC_INTERFACE = "org.bluez.GattCharacteristic1"
        private const val SERVICE_PRIMARY_PROPERTY_KEY = "Primary"

        private const val CHARACTERISTIC_SERVICE_PROPERTY_KEY = "Service"
        private const val CHARACTERISTIC_UUID_PROPERTY_KEY = "UUID"
        private const val CHARACTERISTIC_FLAGS_PROPERTY_KEY = "Flags"
        private const val CHARACTERISTIC_DESCRIPTORS_PROPERTY_KEY = "Descriptors"
        private const val CHARACTERISTIC_VALUE_PROPERTY_KEY = "Value"

        private const val CHARACTERISTIC_FLAG_READ = "read"
        private const val CHARACTERISTIC_FLAG_WRITE = "write"
        private const val CHARACTERISTIC_FLAG_NOTIFY = "notify"
    }

    private val dbusConnector = DBusConnectionBuilder.forSystemBus().build()

    fun startServices(bleServices: List<BLEService>, adapterAlias: String? = null, serverName: String) {
        val adapterPath = findAdapterPath()

        val adapterProperties =
            dbusConnector.getRemoteObject(BLUEZ_DBUS_BUS_NAME, adapterPath, Properties::class.java) as Properties

        adapterProperties.Set(BLUEZ_ADAPTER_INTERFACE, "Powered", Variant(true))
        if (adapterAlias != null) {
            adapterProperties.Set(BLUEZ_ADAPTER_INTERFACE, "Alias", Variant(adapterAlias))
        }

        val gattManager: GattManager1 = dbusConnector.getRemoteObject(
            BLUEZ_DBUS_BUS_NAME,
            adapterPath,
            GattManager1::class.java
        ) //as GattManager1
        val advManager: LEAdvertisingManager1 = dbusConnector.getRemoteObject(
            BLUEZ_DBUS_BUS_NAME,
            adapterPath,
            LEAdvertisingManager1::class.java
        ) as LEAdvertisingManager1

        registerAdvertisement(
            bleServices = bleServices,
            serverName = serverName,
            advertisementManager = advManager
        )

        registerGattService(
            bleServices = bleServices,
            gattManager = gattManager
        )
    }

    private fun registerGattService(
        bleServices: List<BLEService>,
        gattManager: GattManager1
    ) {
        val gattApplication = object : GattApplication1 {
            override fun getObjectPath(): String {
                return PATH_DBUS_ROOT
            }

            override fun GetManagedObjects(): Map<DBusPath, Map<String, Map<String, Variant<Any>>>> {
                println("Application -> GetManagedObjects")

                return bleServices.associate { service ->
                    val serverPath = service.toPath()
                    val characteristicProperties = service.characteristics.associate { characteristic ->
                        characteristic.toPath(serverPath) to mapOf<String, Variant<Any>>()
                    }
                    serverPath to characteristicProperties
                }
            }

        }

        gattManager.RegisterApplication(gattApplication, mapOf())
    }


    fun stopServices(serverName: String) {
        val path = PATH_DBUS_ROOT + serverName
        unRegisterAdvertisement(path)
    }

    private fun registerAdvertisement(
        bleServices: List<BLEService>,
        serverName: String,
        advertisementManager: LEAdvertisingManager1
    ) {
        val path = PATH_DBUS_ROOT + serverName
        val advertisementProperties = getDBusProperties(
            bleServices = bleServices,
            serverName = serverName,
        )
        dbusConnector.exportObject(
            path,
            advertisementProperties
        )
        advertisementManager.RegisterAdvertisement(advertisementProperties, mutableMapOf())
    }

    private fun unRegisterAdvertisement(path: String) {
        dbusConnector.unExportObject(path)
    }

    private fun getDBusProperties(bleServices: List<BLEService>, serverName: String): DBusInterface {
        return object : Properties, LEAdvertisement1 {
            val properties = buildMap {
                val advertisementProperties = buildMap<String, Variant<*>> {
                    put(ADVERTISEMENT_TYPE_PROPERTY_KEY,Variant(ADVERTISEMENT_TYPE_PERIPHERAL))

                    val serviceUUIDs = bleServices.map {
                        it.uuid.toString()
                    }
                    put(ADVERTISEMENT_SERVICES_UUIDS_PROPERTY_KEY, Variant(serviceUUIDs.toTypedArray()))
                    put(ADVERTISEMENT_LOCAL_NAME_PROPERTY_KEY, Variant(serverName))
                    put(ADVERTISEMENT_INCLUDE_TX_POWER_PROPERTY_KEY, Variant(true))
                }.toMutableMap()
                put(LEADVERTISEMENT_INTERFACE, advertisementProperties)

            }.toMutableMap()


            override fun getObjectPath(): String {
                return serverName + PATH_ADVERTISEMENT_SUFFIX
            }

            override fun Release() {
                println("Release LE Server")
            }

            override fun <A : Any?> Get(interfaceName: String, propertyName: String): A {
                return (properties[interfaceName]?.get(propertyName) as? A)
                    ?: throw RuntimeException("Incompatible types")
            }

            override fun <A : Any?> Set(interfaceName: String, propertyName: String, propertyValue: A) {
                if (propertyValue is Variant<*>) {
                    properties[interfaceName]?.put(propertyName, propertyValue)
                }
            }

            override fun GetAll(interfaceName: String): MutableMap<String, Variant<*>> {
                return properties[interfaceName]
                    ?: throw RuntimeException("Wrong interface [interface_name=$interfaceName]")
            }
        }
    }

    private fun findAdapterPath(): String {
        val bluezObjectManager =
            dbusConnector.getRemoteObject(
                BLUEZ_DBUS_BUS_NAME,
                PATH_DBUS_ROOT,
                ObjectManager::class.java
            ) as ObjectManager

        bluezObjectManager.GetManagedObjects().entries.forEach {
            println("${it.key} -----")
            it.value.entries.forEach {
                println("     ---- ${it.key}")
            }
            println("--------")
        }

        return bluezObjectManager.GetManagedObjects().entries.find { entry ->
            entry.value.containsKey(BLUEZ_LE_ADV_INTERFACE)
                    && entry.value.containsKey(BLUEZ_GATT_INTERFACE)

        }?.key?.path ?: throw RuntimeException("No BLE adapter found")
    }

    private fun BLEService.toPath(): DBusPath {
        return DBusPath("$PATH_DBUS_ROOT$name$PATH_SERVICE_SUFFIX")
    }

    private fun BLECharacteristic.toPath(serverPath: DBusPath): String {
        return "${serverPath.path}$PATH_CHARACTERISTIC_SUFFIX"
    }

    private fun BLECharacteristic.toProperties(
        serverPath: String,
    ): Map<String, Map<String, Variant<*>>> {
        val characteristicProperties = buildMap {
            put(CHARACTERISTIC_SERVICE_PROPERTY_KEY, Variant(serverPath))
            put(CHARACTERISTIC_UUID_PROPERTY_KEY, Variant(uuid))
            put(CHARACTERISTIC_FLAGS_PROPERTY_KEY, Variant(toFlags()))
            put(CHARACTERISTIC_DESCRIPTORS_PROPERTY_KEY, Variant(arrayOf<DBusPath>()))
        }

        return mapOf(GATT_CHARACTERISTIC_INTERFACE to characteristicProperties)
    }

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
}
