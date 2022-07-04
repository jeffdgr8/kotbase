package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLLogDomain
import cocoapods.CouchbaseLite.CBLLogLevel
import cocoapods.CouchbaseLite.CBLLoggerProtocol
import platform.darwin.NSObject

internal fun Logger.convert(): CBLLoggerProtocol = object : NSObject(), CBLLoggerProtocol {

    override fun level(): CBLLogLevel {
        return this@convert.level.actual
    }

    override fun logWithLevel(level: CBLLogLevel, domain: CBLLogDomain, message: String) {
        this@convert.log(LogLevel.from(level), LogDomain.from(domain), message)
    }
}
