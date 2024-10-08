package org.kikermo.bleserver.dsl

import org.kikermo.bleserver.BLEConnectionListener

@BleDsl
class BLEConnectionListenerBuilder {
    var onDeviceConnected: (deviceName: String, deviceAddress: String) -> Unit = { _, _ -> }
    var onDeviceDisconnected: () -> Unit = {}

    fun build(): BLEConnectionListener =
        object : BLEConnectionListener {
            override fun onDeviceConnected(
                deviceName: String,
                deviceAddress: String,
            ) {
                onDeviceConnected.invoke(deviceName, deviceAddress)
            }

            override fun onDeviceDisconnected() {
                onDeviceDisconnected.invoke()
            }
        }
}
