package org.kikermo.bleserver.dsl

import org.kikermo.bleserver.BLEConnectionListener
import org.kikermo.bleserver.BLEServer
import org.kikermo.bleserver.BLEServerConnector
import org.kikermo.bleserver.BLEService
import org.kikermo.bleserver.exception.BLEBuilderException

class BLEServerBuilder {
    var serverName: String? = null
    var bleServerConnector: BLEServerConnector? = null
    var autoStart: Boolean = true

    private var connectionListener: BLEConnectionListener? = null
    private var primaryService: BLEService? = null

    private var services: MutableList<BLEService> = mutableListOf()

    fun service(block: BLEServiceBuilder.() -> Unit) {
        services.add(BLEServiceBuilder().apply(block).build())
    }

    fun primaryService(block: BLEServiceBuilder.() -> Unit) {
        val primaryService = BLEServiceBuilder().apply(block).build()
        this.primaryService = primaryService
        services.add(primaryService)
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

        if(autoStart) {
            start()
        }
    }
}

fun bleServer(block: BLEServerBuilder.() -> Unit): BLEServer = BLEServerBuilder().apply(block).build()
