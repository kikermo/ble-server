package org.kikermo.bleserver

import java.util.UUID

class BLEService(
    val uuid: UUID,
    val characteristics: List<BLECharacteristic>
)
