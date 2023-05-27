package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLConcurrencyControl
import cocoapods.CouchbaseLite.CBLConcurrencyControl.kCBLConcurrencyControlFailOnConflict
import cocoapods.CouchbaseLite.CBLConcurrencyControl.kCBLConcurrencyControlLastWriteWins

public actual enum class ConcurrencyControl {
    LAST_WRITE_WINS,
    FAIL_ON_CONFLICT;

    public val actual: CBLConcurrencyControl
        get() = when (this) {
            LAST_WRITE_WINS -> kCBLConcurrencyControlLastWriteWins
            FAIL_ON_CONFLICT -> kCBLConcurrencyControlFailOnConflict
        }

    internal companion object {

        internal fun from(value: CBLConcurrencyControl): ConcurrencyControl {
            return when (value) {
                kCBLConcurrencyControlLastWriteWins -> LAST_WRITE_WINS
                kCBLConcurrencyControlFailOnConflict -> FAIL_ON_CONFLICT
                else -> error("Unexpected CBLConcurrencyControl")
            }
        }
    }
}
