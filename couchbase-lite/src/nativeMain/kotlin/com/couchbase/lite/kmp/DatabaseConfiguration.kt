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
internal constructor(public actual var directory: String) {

    public actual constructor(config: DatabaseConfiguration?) : this(
        config?.directory
            ?: CBLDatabaseConfiguration_Default().useContents {
                directory.toKString()!!.dropLastWhile { it == '/' }
            }
    )

    internal constructor(actual: CValue<CBLDatabaseConfiguration>) : this(
        actual.useContents { directory.toKString()!!.dropLastWhile { it == '/' } }
    )

    public actual fun setDirectory(directory: String): DatabaseConfiguration {
        this.directory = directory
        return this
    }

    internal fun getActual(): CValue<CBLDatabaseConfiguration> {
        val dir = directory
        return cValue {
            with(this.directory) {
                buf = strdup(dir)
                size = strlen(dir)
            }
        }
    }
}
