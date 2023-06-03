package kotbase.util

import kotlin.native.identityHashCode

public fun Any?.identityHashCodeHex(): String =
    "0x${identityHashCode().toUInt().toString(16)}"
