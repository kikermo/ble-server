package org.kikermo.bleserver.dsl

import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.BLEService
import org.kikermo.bleserver.exception.BLEBuilderException
import java.util.UUID

class BLEServiceBuilder {
    var uuid: UUID? = null
    var name: String? = null
    var characteristics: List<BLECharacteristic> = listOf()

    fun build(): BLEService = BLEService(
        uuid = uuid ?: throw BLEBuilderException(childComponent = "uuid", component = "BLEService"),
        name = name ?: throw BLEBuilderException(childComponent = "name", component = "BLEService"),
        characteristics = characteristics
    )
}

fun bleService(block: BLEServiceBuilder.() -> Unit): BLEService = BLEServiceBuilder().apply(block).build()