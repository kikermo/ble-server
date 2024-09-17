package org.kikermo.bleserver.bluez.dsl

import org.kikermo.bleserver.bluez.BluezBLEServerConnector
import org.kikermo.bleserver.dsl.BLEServerBuilder

fun BLEServerBuilder.bluezServerConnector() {
    this.bleServerConnector = BluezBLEServerConnector()
}
