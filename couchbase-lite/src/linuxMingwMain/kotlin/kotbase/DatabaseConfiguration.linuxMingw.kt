/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotbase.internal.fleece.toKString
import kotlinx.cinterop.*
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

    public actual constructor() : this(null)

    private val memory = object {
        val arena = Arena()
        val actual = arena.alloc<CBLDatabaseConfiguration>().ptr
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        free(it.actual.pointed.directory.buf)
        it.arena.clear()
    }

    internal val actual: CPointer<CBLDatabaseConfiguration>
        get() = memory.actual

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

    public actual fun setFullSync(fullSync: Boolean): DatabaseConfiguration {
        actual.pointed.fullSync = fullSync
        return this
    }

    public actual var isFullSync: Boolean
        get() = actual.pointed.fullSync
        set(value) {
            actual.pointed.fullSync = value
        }

    public actual fun setMMapEnabled(mmapEnabled: Boolean): DatabaseConfiguration {
        actual.pointed.mmapDisabled = !mmapEnabled
        return this
    }

    public actual var isMMapEnabled: Boolean
        get() = !actual.pointed.mmapDisabled
        set(value) {
            actual.pointed.mmapDisabled = !value
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
