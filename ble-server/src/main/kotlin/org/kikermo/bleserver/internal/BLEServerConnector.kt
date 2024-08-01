package org.kikermo.bleserver.internal

import org.bluez.*
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.*
import org.freedesktop.dbus.types.Variant
import org.kikermo.bleserver.BLEService

internal class BLEServerConnector {
    companion object {

        private const val DBUS_BUSNAME = "org.freedesktop.DBus"

        private const val BLUEZ_DBUS_BUS_NAME = "org.bluez"
        private const val BLUEZ_GATT_INTERFACE = "org.bluez.GattManager1"
        private const val BLUEZ_LE_ADV_INTERFACE = "org.bluez.LEAdvertisingManager1"
        private const val BLUEZ_ADAPTER_INTERFACE = "org.bluez.Adapter1"
        private const val BLUEZ_DEVICE_INTERFACE = "org.bluez.Device1"


        private const val PATH_DBUS_DIVIDER = "/"
        private const val PATH_ADVERTISEMENT_SUFFIX = "/advertisement"

        private const val GATT_SERVICE_INTERFACE = "org.bluez.GattService1"
    }

    private val dbusConnector = DBusConnectionBuilder.forSystemBus().build()

    private lateinit var interfacesAddedSignalHandler: DBusSigHandler<ObjectManager.InterfacesAdded>
    private lateinit var interfacesRemovedSignalHandler: DBusSigHandler<ObjectManager.InterfacesRemoved>

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
        ) as GattManager1
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
            gattManager = gattManager,
            applicationName = serverName
        )

        initInterfacesHandler()
    }

    private fun registerAdvertisement(
        bleServices: List<BLEService>,
        serverName: String,
        advertisementManager: LEAdvertisingManager1
    ) {
        val advertisementProperties = getAdvertisementProperties(
            bleServices = bleServices,
            serverName = serverName,
        )
        dbusConnector.exportObject(
            advertisementProperties
        )
        advertisementManager.RegisterAdvertisement(advertisementProperties, mapOf())
    }

    private fun registerGattService(
        bleServices: List<BLEService>,
        gattManager: GattManager1,
        applicationName: String,
    ) {
        val managedObject = buildMap {
            bleServices.forEachIndexed { index, bleService ->
                val servicePath = bleService.toPath(applicationName)
                put(
                    key = servicePath,
                    value = bleService.toProperties(isPrimary = index == 0, serverName = applicationName)
                )  // TODO define primary service in API

                bleService.characteristics.forEach { bleCharacteristic ->
                    put(
                        key = DBusPath(bleCharacteristic.toPath(servicePath.path)),
                        value = bleCharacteristic.toProperties(servicePath.path)
                    )
                }
            }
        }

        val gattApplication = object : GattApplication1 {
            override fun isRemote(): Boolean {
                return false
            }

            override fun getObjectPath(): String {
                return "$PATH_DBUS_DIVIDER$applicationName"
            }

            override fun GetManagedObjects(): Map<DBusPath, Map<String, Map<String, Variant<*>>>> {
                println("Application -> GetManagedObjects")

                return managedObject
            }
        }
        bleServices.forEach { bleService ->
            val gattService = gattService1(bleService = bleService, applicationName = applicationName)
            bleService.characteristics.forEach { bleCharacteristic ->
                val gattCharacteristic = object : GattCharacteristic1, Properties {
                    override fun getObjectPath(): String {
                        return bleCharacteristic.toPath(bleService.toPath(applicationName).path)
                    }

                    override fun <A : Any?> Get(p0: String?, p1: String?): A {
                        TODO("Not yet implemented")
                    }

                    override fun <A : Any?> Set(p0: String?, p1: String?, p2: A) {
                        TODO("Not yet implemented")
                    }

                    override fun GetAll(interfaceName: String): Map<String, Variant<*>> {
                        if (GATT_CHARACTERISTIC_INTERFACE == interfaceName) {
                            return bleCharacteristic.toProperties(gattService.objectPath)[GATT_CHARACTERISTIC_INTERFACE]
                                ?: throw RuntimeException("Interface [interface_name=$interfaceName]")
                        }
                        throw RuntimeException("Interface [interface_name=$interfaceName]")
                    }

                    override fun ReadValue(option: MutableMap<String, Variant<Any>>?): ByteArray {
                        return byteArrayOf()
                    }

                    override fun WriteValue(value: ByteArray?, option: MutableMap<String, Variant<Any>>?) {
                    }

                    override fun StartNotify() {

                    }

                    override fun StopNotify() {
                    }

                }
                dbusConnector.exportObject(gattCharacteristic)
            }
            dbusConnector.exportObject(gattService)
        }

        dbusConnector.exportObject(gattApplication)

        gattApplication.javaClass.methods.forEach {
            println(it.name)
            println(it.toString())
        }

        gattManager.javaClass.methods.forEach {
            println(it.name)
            println(it.toString())
        }

        gattManager.RegisterApplication(gattApplication, mutableMapOf())
    }

    private fun gattService1(bleService: BLEService, applicationName: String) = object : GattService1, Properties {
        override fun getObjectPath(): String {
            return bleService.toPath(applicationName).path
        }

        override fun <A : Any?> Get(p0: String?, p1: String?): A {
            TODO("Not yet implemented")
        }

        override fun <A : Any?> Set(p0: String?, p1: String?, p2: A) {
            TODO("Not yet implemented")
        }

        override fun GetAll(interfaceName: String): Map<String, Variant<*>> {
            if (GATT_SERVICE_INTERFACE == interfaceName) {
                return bleService.toProperties(true, applicationName)[GATT_SERVICE_INTERFACE]
                    ?: throw RuntimeException("No $GATT_SERVICE_INTERFACE found on service")
            }
            throw RuntimeException("Interface $interfaceName doesn't match $GATT_SERVICE_INTERFACE")
        }
    }


    fun stopServices(serverName: String) {
        val path = PATH_DBUS_DIVIDER + serverName
        unRegisterAdvertisement(path)
    }

    private fun unRegisterAdvertisement(path: String) {
        dbusConnector.unExportObject(path)
    }

    private fun getAdvertisementProperties(bleServices: List<BLEService>, serverName: String): DBusInterface {
        return object : Properties, LEAdvertisement1 {
            val properties = toAdvertisementProperties(bleServices = bleServices, serverName = serverName)

            override fun getObjectPath(): String {
                return "$PATH_DBUS_DIVIDER$serverName$PATH_ADVERTISEMENT_SUFFIX"
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
                PATH_DBUS_DIVIDER,
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

    private fun initInterfacesHandler() {
        val dbus: DBus = dbusConnector.getRemoteObject(
            DBUS_BUSNAME, "/or/freedesktop/DBus",
            DBus::class.java
        )
        val bluezDbusBusName: String = dbus.GetNameOwner(BLUEZ_DBUS_BUS_NAME)
        val bluezObjectManager = dbusConnector.getRemoteObject(
            BLUEZ_DBUS_BUS_NAME, "/",
            ObjectManager::class.java
        )

        interfacesAddedSignalHandler =
            DBusSigHandler<ObjectManager.InterfacesAdded> { signal ->
                val iamap: Map<String, Variant<*>>? = signal.interfaces.get(BLUEZ_DEVICE_INTERFACE)
                if (iamap != null) {
                    val address: Variant<String> = iamap["Address"] as Variant<String>
                    println("Device address: " + address.value)
                    println("Device added path: " + signal.getObjectPath().toString())
//                    hasDeviceConnected = true
//                    if (listener != null) {
//                        listener.deviceConnected()
//                    }
                }
            }

        interfacesRemovedSignalHandler =
            DBusSigHandler<ObjectManager.InterfacesRemoved> { signal ->
                signal.interfaces.filter { it == BLUEZ_DEVICE_INTERFACE }.forEach { ir ->
                    println("Device Removed path: " + signal.getObjectPath().toString())
//                    hasDeviceConnected = false
//                    if (listener != null) {
//                        listener.deviceDisconnected()
//                    }
                }
            }

        dbusConnector.addSigHandler(
            ObjectManager.InterfacesAdded::class.java,
            bluezDbusBusName,
            bluezObjectManager,
            interfacesAddedSignalHandler
        )
        dbusConnector.addSigHandler(
            ObjectManager.InterfacesRemoved::class.java,
            bluezDbusBusName,
            bluezObjectManager,
            interfacesRemovedSignalHandler
        )
    }
}
