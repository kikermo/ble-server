package org.kikermo.bleserver

import java.util.UUID

class BLEService(
    val uuid: UUID,
    val name: String,
    val characteristics: List<BLECharacteristic>
)
