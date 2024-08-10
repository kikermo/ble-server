package org.kikermo.bleserver.bluez

import org.kikermo.bleserver.BLEConnectionListener
import org.kikermo.bleserver.BLEServerConnector
import org.kikermo.bleserver.BLEService
import org.kikermo.bleserver.bluez.internal.BluezBLEConnector

class BluezBLEServerConnector : BLEServerConnector {
    private val connector = BluezBLEConnector()

    override fun startServices(
        bleServices: List<BLEService>,
        adapterAlias: String?,
        serverName: String,
        primaryService: BLEService,
        listener: BLEConnectionListener?
    ) {
        connector.startServices(
            bleServices = bleServices,
            adapterAlias = adapterAlias,
            serverName = serverName,
            primaryService = primaryService,
            listener = listener,
        )
    }

    override fun stopServices(serverName: String) {
        connector.stopServices(serverName)
    }
}