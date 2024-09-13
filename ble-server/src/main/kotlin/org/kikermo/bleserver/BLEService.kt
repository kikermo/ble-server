package org.kikermo.bleserver

import java.util.UUID

class BLEService(
    val uuid: UUID,
    val name: String,
    val characteristics: List<BLECharacteristic>
) {
//    private constructor(builder: Builder) : this(
//        uuid = builder.uuid ?: throw BLEBuilderException(childComponent = "uuid", component = "BLEService"),
//        name = builder.name ?: throw BLEBuilderException(childComponent = "name", component = "BLEService"),
//        characteristics = builder.characteristics
//    )
//
//    class Builder {
//        internal var uuid: UUID? = null
//        internal var name: String? = null
//        internal var characteristics: List<BLECharacteristic> = listOf()
//
//        fun uuid(uuid: UUID): Builder {
//            this.uuid = uuid
//            return this
//        }
//
//        fun name(name: String): Builder {
//            this.name = name
//            return this
//        }
//
//        fun characteristics(characteristics: List<BLECharacteristic>): Builder {
//            this.characteristics = characteristics
//            return this
//        }
//
//        fun build(): BLEService = BLEService(this)
//    }
}
