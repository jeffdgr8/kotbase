package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLLogFileConfiguration
import com.udobny.kmm.DelegatedClass
import kotlinx.cinterop.convert

public actual class LogFileConfiguration
internal constructor(actual: CBLLogFileConfiguration) :
    DelegatedClass<CBLLogFileConfiguration>(actual) {

    public actual constructor(directory: String) : this(CBLLogFileConfiguration(directory))

    public actual constructor(config: LogFileConfiguration) : this(config.directory, config)

    public actual constructor(directory: String, config: LogFileConfiguration?) : this(directory) {
        config?.let {
            setMaxRotateCount(it.maxRotateCount)
            setMaxSize(it.maxSize)
            setUsePlaintext(it.usesPlaintext)
        }
    }

    public actual fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration = chain {
        setUsePlainText(usePlaintext)
    }

    public actual var maxRotateCount: Int
        get() = actual.maxRotateCount.toInt()
        set(value) {
            actual.maxRotateCount = value.convert()
        }

    public actual fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration = chain {
        setMaxRotateCount(maxRotateCount.convert())
    }

    public actual var maxSize: Long
        get() = actual.maxSize.toLong()
        set(value) {
            actual.maxSize = value.convert()
        }

    public actual fun setMaxSize(maxSize: Long): LogFileConfiguration = chain {
        setMaxSize(maxSize.convert())
    }

    public actual var usesPlaintext: Boolean
        get() = actual.usePlainText
        set(value) {
            actual.usePlainText = value
        }

    public actual val directory: String
        get() = actual.directory
}

internal fun CBLLogFileConfiguration.asLogFileConfiguration() = LogFileConfiguration(this)
