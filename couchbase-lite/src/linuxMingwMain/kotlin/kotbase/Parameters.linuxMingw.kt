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

import kotbase.internal.fleece.*
import kotlin.time.Instant
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Parameters
internal constructor(
    actual: FLDict,
    readonly: Boolean
) {

    private val memory = object {
        val actual = actual
        val readonly = readonly
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        // don't release FLDict from Query.parameters
        if (!it.readonly) debug.FLDict_Release(it.actual)
    }

    public actual constructor() : this(null)

    public actual constructor(parameters: Parameters?) : this(
        if (parameters != null) {
            debug.FLDict_MutableCopy(parameters.actual, kFLDefaultCopy)
        } else {
            debug.FLMutableDict_New()
        }!!,
        false
    )

    internal val actual: FLDict
        get() = memory.actual

    private val readonly: Boolean
        get() = memory.readonly

    public actual fun getValue(name: String): Any? =
        actual.getValue(name)?.toNative(null, release = !readonly)

    public actual fun setString(name: String, value: String?): Parameters {
        checkReadOnly()
        actual.setString(name, value)
        return this
    }

    public actual fun setNumber(name: String, value: Number?): Parameters {
        checkReadOnly()
        actual.setNumber(name, value)
        return this
    }

    public actual fun setInt(name: String, value: Int): Parameters {
        checkReadOnly()
        actual.setInt(name, value)
        return this
    }

    public actual fun setLong(name: String, value: Long): Parameters {
        checkReadOnly()
        actual.setLong(name, value)
        return this
    }

    public actual fun setFloat(name: String, value: Float): Parameters {
        checkReadOnly()
        actual.setFloat(name, value)
        return this
    }

    public actual fun setDouble(name: String, value: Double): Parameters {
        checkReadOnly()
        actual.setDouble(name, value)
        return this
    }

    public actual fun setBoolean(name: String, value: Boolean): Parameters {
        checkReadOnly()
        actual.setBoolean(name, value)
        return this
    }

    public actual fun setDate(name: String, value: Instant?): Parameters {
        checkReadOnly()
        actual.setDate(name, value)
        return this
    }

    public actual fun setBlob(name: String, value: Blob?): Parameters {
        checkReadOnly()
        actual.setBlob(name, value, null)
        return this
    }

    public actual fun setDictionary(name: String, value: Dictionary?): Parameters {
        checkReadOnly()
        actual.setDictionary(name, value, null)
        return this
    }

    public actual fun setArray(name: String, value: Array?): Parameters {
        checkReadOnly()
        actual.setArray(name, value, null)
        return this
    }

    public actual fun setValue(name: String, value: Any?): Parameters {
        checkReadOnly()
        actual.setValue(name, value, null)
        return this
    }

    private fun checkReadOnly() {
        if (readonly) throw CouchbaseLiteError("Parameters is readonly mode.")
    }
}

internal fun FLDict.asParameters() = Parameters(this, true)
