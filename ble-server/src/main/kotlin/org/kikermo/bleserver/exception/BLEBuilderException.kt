package org.kikermo.bleserver.exception

class BLEBuilderException(
    childComponent: String,
    component: String
) : RuntimeException("$component requires $childComponent")
