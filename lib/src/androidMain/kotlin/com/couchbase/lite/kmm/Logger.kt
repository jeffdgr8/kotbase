package com.couchbase.lite.kmm

internal fun Logger.convert() = object : com.couchbase.lite.Logger {

    override fun getLevel(): LogLevel {
        return this@convert.level
    }

    override fun log(level: LogLevel, domain: com.couchbase.lite.LogDomain, message: String) {
        this@convert.log(level, LogDomain.from(domain), message)
    }
}
