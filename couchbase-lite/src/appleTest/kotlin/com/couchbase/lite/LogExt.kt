package com.couchbase.lite

import com.couchbase.lite.kmp.Log
import com.couchbase.lite.kmp.LogDomain
import com.couchbase.lite.kmp.LogLevel

internal actual fun Log.reset() {
    console.apply {
        domains = LogDomain.ALL_DOMAINS
        level = LogLevel.WARNING
    }
    file.apply {
        config = null
        level = LogLevel.NONE
    }
    custom = null
}
