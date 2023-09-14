package kotbase

import kotbase.internal.fleece.toKString
import kotlinx.cinterop.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemPathSeparator
import libcblite.CBLDatabaseConfiguration
import libcblite.CBLDatabaseConfiguration_Default
import platform.posix.free
import platform.posix.strdup
import platform.posix.strlen
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class DatabaseConfiguration
public actual constructor(config: DatabaseConfiguration?) {

    private val arena = Arena()

    public val actual: CPointer<CBLDatabaseConfiguration> =
        arena.alloc<CBLDatabaseConfiguration>().ptr

    private val memory = object {
        val arena = this@DatabaseConfiguration.arena
        val actual = this@DatabaseConfiguration.actual
    }

    @OptIn(ExperimentalNativeApi::class)
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
            directory.toKString()!!.dropLastWhile { it == SystemPathSeparator }
        }

    internal var readonly: Boolean = false

    private fun checkReadOnly() {
        if (readonly) throw IllegalStateException("DatabaseConfiguration is readonly mode.")
    }
}
