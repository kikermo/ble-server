package org.kikermo.bleserver.dsl

import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.BLEService
import org.kikermo.bleserver.exception.BLEBuilderException
import java.util.UUID

class BLEServiceBuilder {
    var uuid: UUID? = null
    var name: String? = null
    private var characteristics: MutableList<BLECharacteristic> = mutableListOf()

//    fun characteristics(block: CHARACTERISTICS.() -> Unit) {
//        characteristics = CHARACTERISTICS().apply(block).toList()
//    }

    fun characteristic(block: BLECharacteristicBuilder.() -> Unit) {
       characteristics.add(BLECharacteristicBuilder().apply(block).build())
    }

    fun build(): BLEService = BLEService(
        uuid = uuid ?: throw BLEBuilderException(childComponent = "uuid", component = "BLEService"),
        name = name ?: throw BLEBuilderException(childComponent = "name", component = "BLEService"),
        characteristics = characteristics
    )
}

fun bleService(block: BLEServiceBuilder.() -> Unit): BLEService = BLEServiceBuilder().apply(block).build()

//class CHARACTERISTICS : ArrayList<BLECharacteristic>() {
//    fun characteristic(block: BLECharacteristicBuilder.() -> Unit) {
//        add(BLECharacteristicBuilder().apply(block).build())
//    }
//}
