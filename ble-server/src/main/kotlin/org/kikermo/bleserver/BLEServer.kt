package org.kikermo.bleserver

import org.kikermo.bleserver.exception.BLENoServicesException

// AKA BLE Application
class BLEServer(
    private val serverName: String,
    private val services: List<BLEService>,
    private val connectionListener: BLEConnectionListener? = null,
    private val bleServerConnector: BLEServerConnector,
) {
    private var selectedPrimaryService: BLEService? = null

    var primaryService: BLEService
        get() = selectedPrimaryService ?: services.firstOrNull() ?: throw BLENoServicesException()
        set(value) {
            selectedPrimaryService = value
        }

    fun start() {
        bleServerConnector.startServices(
            bleServices = services,
            primaryService = primaryService,
            serverName = serverName,
            listener = connectionListener,
        )
    }

    fun stop() {
        bleServerConnector.stopServices(serverName)
    }
}
