package org.kikermo.bleserver.dsl

import org.kikermo.bleserver.BLEConnectionListener
import org.kikermo.bleserver.BLEServer
import org.kikermo.bleserver.BLEServerConnector
import org.kikermo.bleserver.BLEService
import org.kikermo.bleserver.exception.BLEBuilderException

class BLEServerBuilder {
    var serverName: String? = null
    private var connectionListener: BLEConnectionListener? = null
    var bleServerConnector: BLEServerConnector? = null
    private var primaryService: BLEService? = null

    private var services: MutableList<BLEService> = mutableListOf()

    fun service(block: BLEServiceBuilder.() -> Unit) {
        services.add(BLEServiceBuilder().apply(block).build())
    }

    fun primaryService(block: BLEServiceBuilder.() -> Unit) {
        primaryService = BLEServiceBuilder().apply(block).build()
    }

    fun connectionListener(block: BLEConnectionListenerBuilder.() -> Unit) {
        this.connectionListener = BLEConnectionListenerBuilder().apply(block).build()
    }

    fun build(): BLEServer = BLEServer(
        serverName = this.serverName ?: throw BLEBuilderException(
            childComponent = "serviceName",
            component = "BLEServer"
        ),
        bleServerConnector = this.bleServerConnector ?: throw BLEBuilderException(
            childComponent = "bleServerConnector",
            component = "BLEServer"
        ),
        connectionListener = this.connectionListener,
        services = services.toList()
    ).apply {
        this@BLEServerBuilder.primaryService?.let { builderPrimaryService ->
            primaryService = builderPrimaryService
        }
    }
}

fun bleServer(block: BLEServerBuilder.() -> Unit): BLEServer = BLEServerBuilder().apply(block).build()

//class SERVICES : ArrayList<BLEService>() {
//    fun service(block: BLEServiceBuilder.() -> Unit) {
//        add(BLEServiceBuilder().apply(block).build())
//    }
//}