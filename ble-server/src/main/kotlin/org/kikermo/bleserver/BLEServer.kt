package org.kikermo.bleserver

import org.kikermo.bleserver.exception.BLENoServicesException
import org.kikermo.bleserver.internal.BLEServerConnector

// AKA BLE Application
class BLEServer(
    private val serverName: String,
    private val services: List<BLEService>,
    private val connectionListener: BLEConnectionListener? = null,
) {
    private val bleServerConnector = BLEServerConnector()

    private var _selectedPrimaryService: BLEService? = null

    var primaryService: BLEService
        get() = _selectedPrimaryService ?: services.firstOrNull() ?: throw BLENoServicesException()
        set(value) {
            _selectedPrimaryService = value
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