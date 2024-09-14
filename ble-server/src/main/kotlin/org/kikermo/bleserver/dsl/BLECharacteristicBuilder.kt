package org.kikermo.bleserver.dsl

import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.BLECharacteristic.AccessType
import org.kikermo.bleserver.exception.BLEBuilderException
import java.util.UUID

class BLECharacteristicBuilder {
    var uuid: UUID? = null
    var name: String? = null

    var readAccess: AccessType.Read? = null
    var writeAccess: AccessType.Write? = null
    var notifyAccess: AccessType.Notify? = null

    fun build() = BLECharacteristic(
        uuid = uuid ?: throw BLEBuilderException(childComponent = "uuid", component = "BLECharacteristic"),
        name = name ?: throw BLEBuilderException(childComponent = "name", component = "BLECharacteristic"),
        readAccess = readAccess,
        writeAccess = writeAccess,
        notifyAccess = notifyAccess
    )
}
