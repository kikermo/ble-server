package org.kikermo.bleserver

// AKA BLE Application
class BLEServer(
    private val services: List<BLEService>,
    private val advertisement: BLEAdvertisement,
    private val connectionListener: BLEConnectionListener? = null
) {
    fun start() {

    }

    fun stop() {

    }
}