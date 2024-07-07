package org.kikermo.bleserver.org.kikermo.bleserver

import java.util.UUID

class BLUCharacteristic(
    val uuid: UUID,
    val readAccess: AccessType.Read?,
    val writeAccess: AccessType.Write?,
    val notifyAccess: AccessType.Notify,
) {

    var onValueChanged: ((ByteArray) -> Unit)? = null
    var value: ByteArray = byteArrayOf()

    sealed class AccessType {
        data object Read : AccessType()
        data object Notify : AccessType()
        data class Write(val onValueChangedListener: (ByteArray) -> Unit) : AccessType()
    }
}