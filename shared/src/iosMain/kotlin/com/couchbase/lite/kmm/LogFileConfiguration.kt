package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLLogFileConfiguration
import com.udobny.kmm.DelegatedClass

public actual class LogFileConfiguration
internal constructor(actual: CBLLogFileConfiguration) :
    DelegatedClass<CBLLogFileConfiguration>(actual) {

    public actual constructor(directory: String) : this(CBLLogFileConfiguration(directory))

    public actual constructor(config: LogFileConfiguration) : this(
        CBLLogFileConfiguration(config.getDirectory())
    ) {
        config.let {
            setMaxRotateCount(it.getMaxRotateCount())
            setMaxSize(it.getMaxSize())
            setUsePlaintext(it.usesPlaintext())
        }
    }

    public actual constructor(directory: String, config: LogFileConfiguration?) : this(directory) {
        config?.let {
            setMaxRotateCount(it.getMaxRotateCount())
            setMaxSize(it.getMaxSize())
            setUsePlaintext(it.usesPlaintext())
        }
    }

    public actual fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration = chain {
        setUsePlainText(usePlaintext)
    }

    public actual fun getMaxRotateCount(): Int =
        actual.maxRotateCount.toInt()

    public actual fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration = chain {
        setMaxRotateCount(maxRotateCount.toLong())
    }

    public actual fun getMaxSize(): Long =
        actual.maxSize.toLong()

    public actual fun setMaxSize(maxSize: Long): LogFileConfiguration = chain {
        setMaxSize(maxSize.toULong())
    }

    public actual fun usesPlaintext(): Boolean =
        actual.usePlainText

    public actual fun getDirectory(): String =
        actual.directory
}

internal fun CBLLogFileConfiguration.asLogFileConfiguration() = LogFileConfiguration(this)
