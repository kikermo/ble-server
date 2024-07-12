package org.kikermo.bleserver

import org.kikermo.bleserver.internal.BLEServerConnector

// AKA BLE Application
class BLEServer(
    private val services: List<BLEService>,
    private val connectionListener: BLEConnectionListener? = null
) {
    private val bleServerConnector = BLEServerConnector()

    fun start() {

    }

    fun stop() {

    }
}