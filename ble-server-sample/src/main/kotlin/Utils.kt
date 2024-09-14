package org.kikermo.bleserver

import java.util.UUID

internal fun String.toUUID() = UUID.fromString(this)
