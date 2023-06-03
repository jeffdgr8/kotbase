package com.couchbase.lite

import kotbase.Log
import kotbase.LogDomain
import kotbase.LogLevel

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
