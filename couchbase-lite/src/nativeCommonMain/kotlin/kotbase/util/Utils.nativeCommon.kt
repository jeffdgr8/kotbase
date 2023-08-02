package kotbase.util

import kotlin.native.identityHashCode

internal fun Any?.identityHashCodeHex(): String =
    "0x${identityHashCode().toUInt().toString(16)}"
