package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.*
import libcblite.CBLDatabaseConfiguration
import libcblite.CBLDatabaseConfiguration_Default
import okio.Path
import platform.posix.free
import platform.posix.strdup
import platform.posix.strlen
import kotlin.native.internal.createCleaner

public actual class DatabaseConfiguration
public actual constructor(config: DatabaseConfiguration?) {

    private val arena = Arena()

    internal val actual: CPointer<CBLDatabaseConfiguration> =
        arena.alloc<CBLDatabaseConfiguration>().ptr

    private val memory = object {
        val arena = this@DatabaseConfiguration.arena
        val actual = this@DatabaseConfiguration.actual
    }

    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        free(it.actual.pointed.directory.buf)
        it.arena.clear()
    }

    public actual fun setDirectory(directory: String): DatabaseConfiguration {
        this.directory = directory
        return this
    }

    public actual var directory: String = config?.directory ?: defaultDirectory
        set(value) {
            checkReadOnly()
            field = value
            setActualDirectory(value)
        }

    init {
        setActualDirectory(directory)
    }

    private fun setActualDirectory(directory: String) {
        with(actual.pointed.directory) {
            free(buf)
            buf = strdup(directory)
            size = strlen(directory)
        }
    }

    private val defaultDirectory: String
        get() = CBLDatabaseConfiguration_Default().useContents {
            directory.toKString()!!.dropLastWhile { it == Path.DIRECTORY_SEPARATOR.first() }
        }

    internal var readonly: Boolean = false

    private fun checkReadOnly() {
        if (readonly) throw IllegalStateException("DatabaseConfiguration is readonly mode.")
    }
}
