package org.kikermo.bleserver.org.kikermo.bleserver

import java.util.UUID

class BLECharacteristic(
    val uuid: UUID,
    val readAccess: AccessType.Read? = null,
    val writeAccess: AccessType.Write? = null,
    val notifyAccess: AccessType.Notify? = null,
) {

    var onValueChanged: ((ByteArray) -> Unit)? = null
    var value: ByteArray = byteArrayOf()

    sealed class AccessType {
        data object Read : AccessType()
        data object Notify : AccessType()
        data class Write(val onValueChangedListener: (ByteArray) -> Unit) : AccessType()
    }
}