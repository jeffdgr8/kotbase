package kotbase.util

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.identityHashCode

@OptIn(ExperimentalNativeApi::class)
internal fun Any?.identityHashCodeHex(): String =
    "0x${identityHashCode().toUInt().toString(16)}"
