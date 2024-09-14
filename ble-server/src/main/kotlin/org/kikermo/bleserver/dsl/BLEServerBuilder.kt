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
    var primaryService :BLEService?= null

    private var services: List<BLEService> = listOf()

    fun services(block: SERVICES.() -> Unit) {
        services = SERVICES().apply(block).toList()
    }

    fun connectionListener(block: BLEConnectionListenerBuilder.() -> Unit) {
        this.connectionListener = BLEConnectionListenerBuilder().apply(block).build()
    }

    fun build(): BLEServer = BLEServer(
        serverName = this.serverName ?: throw BLEBuilderException(childComponent = "serviceName", component = "BLEServer"),
        bleServerConnector = this.bleServerConnector ?: throw BLEBuilderException(
            childComponent = "bleServerConnector",
            component = "BLEServer"
        ),
        connectionListener = this.connectionListener,
        services = (this.services + this.primaryService).filterNotNull()
    ).apply {
        this@BLEServerBuilder.primaryService?.let { builderPrimaryService ->
            primaryService = builderPrimaryService
        }
    }
}

fun bleServer(block: BLEServerBuilder.() -> Unit): BLEServer = BLEServerBuilder().apply(block).build()

class SERVICES : ArrayList<BLEService>() {
    fun service(block: BLEServiceBuilder.() -> Unit) {
        add(BLEServiceBuilder().apply(block).build())
    }
}