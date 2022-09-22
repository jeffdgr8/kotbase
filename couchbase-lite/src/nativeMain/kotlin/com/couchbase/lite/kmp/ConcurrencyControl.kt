package com.couchbase.lite.kmp

import kotlinx.cinterop.convert
import libcblite.CBLConcurrencyControl
import libcblite.kCBLConcurrencyControlFailOnConflict
import libcblite.kCBLConcurrencyControlLastWriteWins

public actual enum class ConcurrencyControl {
    LAST_WRITE_WINS,
    FAIL_ON_CONFLICT;

    internal val actual: CBLConcurrencyControl
        get() = when (this) {
            LAST_WRITE_WINS -> kCBLConcurrencyControlLastWriteWins.convert()
            FAIL_ON_CONFLICT -> kCBLConcurrencyControlFailOnConflict.convert()
        }

    internal companion object {

        internal fun from(value: CBLConcurrencyControl): ConcurrencyControl {
            return when (value.toUInt()) {
                kCBLConcurrencyControlLastWriteWins -> LAST_WRITE_WINS
                kCBLConcurrencyControlFailOnConflict -> FAIL_ON_CONFLICT
                else -> error("Unexpected CBLConcurrencyControl")
            }
        }
    }
}
