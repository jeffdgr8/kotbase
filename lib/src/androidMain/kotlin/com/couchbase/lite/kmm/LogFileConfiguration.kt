package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class LogFileConfiguration(actual: com.couchbase.lite.LogFileConfiguration) :
    DelegatedClass<com.couchbase.lite.LogFileConfiguration>(actual) {

    public actual constructor(directory: String) : this(
        com.couchbase.lite.LogFileConfiguration(directory)
    )

    public actual constructor(config: LogFileConfiguration) : this(
        com.couchbase.lite.LogFileConfiguration(config.actual)
    )

    public actual constructor(directory: String, config: LogFileConfiguration?) : this(
        com.couchbase.lite.LogFileConfiguration(directory, config?.actual)
    )

    public actual fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration = chain {
        setUsePlaintext(usePlaintext)
    }

    public actual var maxRotateCount: Int
        get() = actual.maxRotateCount
        set(value) {
            actual.maxRotateCount = value
        }

    public actual fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration = chain {
        setMaxRotateCount(maxRotateCount)
    }

    public actual var maxSize: Long
        get() = actual.maxSize
        set(value) {
            actual.maxSize = value
        }

    public actual fun setMaxSize(maxSize: Long): LogFileConfiguration = chain {
        setMaxSize(maxSize)
    }

    public actual var usesPlaintext: Boolean
        get() = actual.usesPlaintext()
        set(value) {
            actual.setUsePlaintext(value)
        }

    public actual val directory: String
        get() = actual.directory
}

internal fun com.couchbase.lite.LogFileConfiguration.asLogFileConfiguration() =
    LogFileConfiguration(this)
