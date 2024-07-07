package org.kikermo.bleserver.org.kikermo.bleserver

interface BLEConnectionListener {
    fun onDeviceConnected(
        deviceName: String,
        deviceAddress: String,
    )

    fun onDeviceDisconnected(
        deviceName: String,
        deviceAddress: String,
    )
}
