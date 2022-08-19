package com.couchbase.lite.kmp

import com.couchbase.lite.internal.CouchbaseLiteInternal

internal actual fun couchbaseLiteReset(state: Boolean) {
    CouchbaseLiteInternal.reset(state)
}
