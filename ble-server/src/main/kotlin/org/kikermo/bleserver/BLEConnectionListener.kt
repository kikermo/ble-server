package org.kikermo.bleserver

interface BLEConnectionListener {
    fun onDeviceConnected(
        deviceName: String,
        deviceAddress: String,
    )

    fun onDeviceDisconnected()
}
