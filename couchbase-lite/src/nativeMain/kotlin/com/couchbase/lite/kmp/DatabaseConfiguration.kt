package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.*
import libcblite.CBLDatabaseConfiguration
import libcblite.CBLDatabaseConfiguration_Default
import platform.posix.free
import platform.posix.strdup
import platform.posix.strlen
import kotlin.native.internal.createCleaner

public actual class DatabaseConfiguration
internal constructor(internal var actual: CValue<CBLDatabaseConfiguration>) {

    private val directoryCstr = object {
        var ptr = actual.useContents {
            val default = CBLDatabaseConfiguration_Default().useContents { directory.buf }
            if (directory.buf != default) directory.buf else null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(directoryCstr) {
        free(it.ptr)
    }

    public actual constructor(config: DatabaseConfiguration?) : this(
        if (config != null) {
            cblDatabaseConfiguration(config.directory)
        } else {
            CBLDatabaseConfiguration_Default()
        }
    )

    public actual fun setDirectory(directory: String): DatabaseConfiguration {
        this.directory = directory
        return this
    }

    public actual var directory: String
        get() = actual.useContents { directory.toKString()!! }
        set(value) {
            free(directoryCstr.ptr)
            actual = cblDatabaseConfiguration(value)
            directoryCstr.ptr = actual.useContents { directory.buf }
        }

    private companion object {

        private fun cblDatabaseConfiguration(directory: String): CValue<CBLDatabaseConfiguration> {
            return cValue {
                with(this.directory) {
                    buf = strdup(directory)
                    size = strlen(directory)
                }
            }
        }
    }
}
