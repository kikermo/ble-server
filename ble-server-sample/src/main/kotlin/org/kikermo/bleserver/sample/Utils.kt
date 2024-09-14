package org.kikermo.bleserver.sample

import java.util.UUID

internal fun String.toUUID() = UUID.fromString(this)
