package org.kikermo.bleserver.bluez.internal

import org.bluez.*
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.*
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged
import org.freedesktop.dbus.types.Variant
import org.kikermo.bleserver.BLEConnectionListener
import org.kikermo.bleserver.BLEService
import org.kikermo.bleserver.internal.GATT_CHARACTERISTIC_INTERFACE
import org.kikermo.bleserver.internal.toAdvertisementProperties
import org.kikermo.bleserver.internal.toPath
import org.kikermo.bleserver.internal.toProperties

internal class BluezBLEConnector {
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

    fun startServices(
        bleServices: List<BLEService>,
        adapterAlias: String? = null,
        serverName: String,
        primaryService: BLEService,
        listener: BLEConnectionListener?
    ) {
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
            applicationName = serverName,
            primaryService = primaryService,
        )

        initInterfacesHandler(listener)
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
        primaryService: BLEService,
    ) {
        val managedObject = buildMap {
            bleServices.forEach { bleService ->
                val servicePath = bleService.toPath(applicationName)
                val isPrimaryService = bleService == primaryService
                put(
                    key = servicePath,
                    value = bleService.toProperties(
                        isPrimary = isPrimaryService,
                        serverName = applicationName
                    )
                )

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
                val characteristicProperties = bleCharacteristic.toProperties(gattService.objectPath)
                val gattCharacteristic = object : GattCharacteristic1, Properties {
                    override fun getObjectPath(): String {
                        return bleCharacteristic.toPath(bleService.toPath(applicationName).path)
                    }

                    override fun <A : Any?> Get(p0: String?, p1: String?): A {
                        return characteristicProperties[p0]?.get(p1)?.value as A?
                            ?: throw RuntimeException("No characteristic")
                    }

                    override fun <A : Any?> Set(p0: String?, p1: String?, p2: A) {
                    }

                    override fun GetAll(interfaceName: String): Map<String, Variant<*>> {
                        if (GATT_CHARACTERISTIC_INTERFACE == interfaceName) {
                            return characteristicProperties[GATT_CHARACTERISTIC_INTERFACE]
                                ?: throw RuntimeException("Interface [interface_name=$interfaceName]")
                        }
                        throw RuntimeException("Interface [interface_name=$interfaceName]")
                    }

                    override fun ReadValue(option: MutableMap<String, Variant<Any>>?): ByteArray {
                        return bleCharacteristic.value
                    }

                    override fun WriteValue(value: ByteArray?, option: MutableMap<String, Variant<Any>>?) {
                        if (value == null) {
                            return
                        }
                        bleCharacteristic.writeAccess?.let {
                            it.onValueChangedListener(value)
                        }
                    }

                    override fun StartNotify() {
                        if (bleCharacteristic.notifyAccess == null) {
                            return
                        }
                        bleCharacteristic.onValueChanged = { value ->
                            val signal = PropertiesChanged(
                                /* _path = */ objectPath,
                                /* _interfaceName = */ GATT_CHARACTERISTIC_INTERFACE,
                                /* _propertiesChanged = */ mapOf("Value" to Variant(value)),
                                /* _propertiesRemoved = */ listOf()
                            )
                            dbusConnector.sendMessage(signal)
                        }
                    }

                    override fun StopNotify() {
                        bleCharacteristic.onValueChanged = null
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

    private fun gattService1(bleService: BLEService, applicationName: String): GattService1 {
        val servicePath = bleService.toPath(applicationName).path
        val serviceProperties = bleService.toProperties(true, applicationName)

        return object : GattService1, Properties {
            override fun getObjectPath(): String {
                return servicePath
            }

            override fun <A : Any?> Get(p0: String?, p1: String?): A {
                return serviceProperties[p0]?.get(p1)?.value as A?
                    ?: throw RuntimeException("Property not found on service")
            }

            override fun <A : Any?> Set(p0: String?, p1: String?, p2: A) {
            }

            override fun GetAll(interfaceName: String): Map<String, Variant<*>> {
                if (GATT_SERVICE_INTERFACE == interfaceName) {
                    return serviceProperties[GATT_SERVICE_INTERFACE]
                        ?: throw RuntimeException("No $GATT_SERVICE_INTERFACE found on service")
                }
                throw RuntimeException("Interface $interfaceName doesn't match $GATT_SERVICE_INTERFACE")
            }
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

        return bluezObjectManager.GetManagedObjects().entries.find { entry ->
            entry.value.containsKey(BLUEZ_LE_ADV_INTERFACE)
                    && entry.value.containsKey(BLUEZ_GATT_INTERFACE)

        }?.key?.path ?: throw RuntimeException("No BLE adapter found")
    }

    private fun initInterfacesHandler(listener: BLEConnectionListener?) {
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
                val iamap: Map<String, Variant<*>>? = signal.interfaces[BLUEZ_DEVICE_INTERFACE]
                iamap?.let {
                    val address: Variant<String> = it["Address"] as Variant<String>
                    val alias: Variant<String> = it["Alias"] as Variant<String>

                    listener?.onDeviceConnected(
                        deviceAddress = address.value,
                        deviceName = alias.value
                    )
                }
            }

        interfacesRemovedSignalHandler =
            DBusSigHandler<ObjectManager.InterfacesRemoved> { signal ->
                println(signal.name)
                println(signal.interfaces.joinToString(","))
                signal.interfaces.filter { it == BLUEZ_DEVICE_INTERFACE }.forEach { ir ->
                    listener?.onDeviceDisconnected()
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
