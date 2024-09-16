package org.kikermo.bleserver.dsl

import org.kikermo.bleserver.BLECharacteristic
import org.kikermo.bleserver.BLECharacteristic.AccessType
import org.kikermo.bleserver.exception.BLEBuilderException
import java.util.UUID
import kotlin.concurrent.thread

@BleDsl
class BLECharacteristicBuilder {
    private var valueChangingAction: (((ByteArray) -> Unit) -> Unit)? = null

    var uuid: UUID? = null
    var name: String? = null
    var initialValue: ByteArray = byteArrayOf(0)

    var readAccess: AccessType.Read? = null
    var writeAccess: AccessType.Write? = null
    var notifyAccess: AccessType.Notify? = null


    fun valueChangingAction(action: (valueSetter: (ByteArray) -> Unit) -> Unit) {
        valueChangingAction = action
    }

    fun build() = BLECharacteristic(
        uuid = uuid ?: throw BLEBuilderException(childComponent = "uuid", component = "BLECharacteristic"),
        name = name ?: throw BLEBuilderException(childComponent = "name", component = "BLECharacteristic"),
        readAccess = readAccess,
        writeAccess = writeAccess,
        notifyAccess = notifyAccess
    ).apply {
        this.value = initialValue
        if (readAccess != null || notifyAccess != null) {
            thread {
                valueChangingAction?.invoke {
                    value = it
                }
            }
        }
    }
}
