package org.kikermo.bleserver.org.kikermo.bleserver

import java.security.Provider.Service

// AKA BLE Application
class BLEServer(
    private val services: List<BLEService>,
    private val advertisement: BLEAdvertisement,
    private val connectionListener: () -> Unit
) {
    fun start(){

    }

    fun stop() {

    }
}