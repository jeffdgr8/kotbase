package com.couchbase.lite.kmp

import kotlinx.cinterop.*
import libcblite.CBLLogFileConfiguration
import platform.posix.strlen
import kotlin.native.internal.createCleaner

public actual class LogFileConfiguration
public actual constructor(public actual val directory: String) {

    private val arena = Arena()

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(arena) {
        arena.clear()
    }

    public actual constructor(config: LogFileConfiguration) : this(config.directory, config)

    public actual constructor(directory: String, config: LogFileConfiguration?) : this(directory) {
        config?.let {
            maxRotateCount = it.maxRotateCount
            maxSize = it.maxSize
            usesPlaintext = it.usesPlaintext
        }
    }

    private var readonly: Boolean = false

    public actual fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration {
        checkReadOnly()
        this.usesPlaintext = usesPlaintext
        return this
    }

    public actual var maxRotateCount: Int = 1
        set(value) {
            checkReadOnly()
            field = value
        }

    public actual fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration {
        checkReadOnly()
        this.maxRotateCount = maxRotateCount
        return this
    }

    public actual var maxSize: Long = 0
        set(value) {
            checkReadOnly()
            field = value
        }

    public actual fun setMaxSize(maxSize: Long): LogFileConfiguration {
        checkReadOnly()
        this.maxSize = maxSize
        return this
    }

    public actual var usesPlaintext: Boolean = false
        set(value) {
            checkReadOnly()
            field = value
        }

    // Objective-C SDK doesn't override these, but Java does and tests expect
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is LogFileConfiguration) {
            return false
        }
        return maxRotateCount == other.maxRotateCount
                && directory == other.directory
                && maxSize == other.maxSize
                && usesPlaintext == other.usesPlaintext
    }

    override fun hashCode(): Int = directory.hashCode()

    internal fun getActual(level: LogLevel): CValue<CBLLogFileConfiguration> {
        if (readonly) {
            // calling multiple times, free previously used memory
            arena.clear()
        }
        readonly = true
        return cValue {
            with(directory) {
                buf = this@LogFileConfiguration.directory.cstr.getPointer(arena)
                size = strlen(this@LogFileConfiguration.directory)
            }
            this.level = level.actual
            maxRotateCount = this@LogFileConfiguration.maxRotateCount.convert()
            maxSize = this@LogFileConfiguration.maxSize.convert()
            usePlaintext = this@LogFileConfiguration.usesPlaintext
        }
    }

    private fun checkReadOnly() {
        if (readonly) throw IllegalStateException("LogFileConfiguration is readonly mode.")
    }
}
