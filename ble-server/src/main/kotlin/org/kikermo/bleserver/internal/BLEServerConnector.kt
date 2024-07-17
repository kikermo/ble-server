package org.kikermo.bleserver.internal

import org.bluez.GattManager1
import org.bluez.LEAdvertisement1
import org.bluez.LEAdvertisingManager1
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.interfaces.ObjectManager
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import org.kikermo.bleserver.BLEService

internal class BLEServerConnector {
    companion object {
        private const val BLUEZ_DBUS_BUS_NAME = "org.bluez"
        private const val BLUEZ_GATT_INTERFACE = "org.bluez.GattManager1"
        private const val BLUEZ_LE_ADV_INTERFACE = "org.bluez.LEAdvertisingManager1"
        private const val BLUEZ_ADAPTER_INTERFACE = "org.bluez.Adapter1"

        private const val ADVERTISEMENT_TYPE_PERIPHERAL = "peripheral"
        private const val ADVERTISEMENT_SERVICES_UUIDS_PROPERTY_KEY = "ServiceUUIDs"
        private const val ADVERTISEMENT_LOCAL_NAME_PROPERTY_KEY = "LocalName"
        private const val ADVERTISEMENT_INCLUDE_TX_POWER_PROPERTY_KEY = "IncludeTxPower"


        private const val PATH_DBUS_ROOT = "/"
        private const val PATH_ADVERTISEMENT_SUFFIX = "/advertisement"

        private const val LEADVERTISEMENT_INTERFACE = "org.bluez.LEAdvertisement1"

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

        val gattManager: DBusInterface = dbusConnector.getRemoteObject(
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
}
