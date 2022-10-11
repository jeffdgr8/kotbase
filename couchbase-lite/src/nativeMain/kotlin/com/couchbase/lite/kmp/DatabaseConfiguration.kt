package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.*
import libcblite.CBLDatabaseConfiguration
import libcblite.CBLDatabaseConfiguration_Default
import okio.Path
import platform.posix.strdup
import platform.posix.strlen

public actual class DatabaseConfiguration
internal constructor(public actual var directory: String) {

    public actual constructor(config: DatabaseConfiguration?) : this(
        config?.directory
            ?: CBLDatabaseConfiguration_Default().useContents {
                directory.toKString()!!.dropLastWhile { it == Path.DIRECTORY_SEPARATOR.first() }
            }
    )

    internal constructor(actual: CValue<CBLDatabaseConfiguration>) : this(
        actual.useContents { directory.toKString()!!.dropLastWhile { it == Path.DIRECTORY_SEPARATOR.first() } }
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
