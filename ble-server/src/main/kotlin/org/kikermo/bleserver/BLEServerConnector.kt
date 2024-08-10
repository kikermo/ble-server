package org.kikermo.bleserver

interface BLEServerConnector {
    fun startServices(
        bleServices: List<BLEService>,
        adapterAlias: String? = null,
        serverName: String,
        primaryService: BLEService,
        listener: BLEConnectionListener?
    )

    fun stopServices(serverName: String)
}
