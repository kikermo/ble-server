package org.kikermo.bleserver

import org.kikermo.bleserver.internal.BLEServerConnector

// AKA BLE Application
class BLEServer(
    private val serverName: String,
    private val services: List<BLEService>,
    private val connectionListener: BLEConnectionListener? = null,
) {
    private val bleServerConnector = BLEServerConnector()

    fun start() {
        bleServerConnector.startServices(
            bleServices = services,
            serverName = serverName
        )
    }

    fun stop() {
        bleServerConnector.stopServices(serverName)
    }
}