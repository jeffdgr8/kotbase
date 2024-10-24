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

import cocoapods.CouchbaseLite.CBLQueryParameters
import kotbase.internal.DelegatedClass
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual class Parameters
internal constructor(actual: CBLQueryParameters) : DelegatedClass<CBLQueryParameters>(actual) {

    public actual constructor() : this(CBLQueryParameters())

    public actual constructor(parameters: Parameters?) : this(
        CBLQueryParameters(parameters?.actual)
    )

    public actual fun getValue(name: String): Any? =
        actual.valueForName(name)?.delegateIfNecessary()

    public actual fun setString(name: String, value: String?): Parameters {
        actual.setString(value, name)
        return this
    }

    public actual fun setNumber(name: String, value: Number?): Parameters {
        actual.setNumber(value as NSNumber?, name)
        return this
    }

    public actual fun setInt(name: String, value: Int): Parameters {
        actual.setInteger(value.convert(), name)
        return this
    }

    public actual fun setLong(name: String, value: Long): Parameters {
        actual.setLongLong(value, name)
        return this
    }

    public actual fun setFloat(name: String, value: Float): Parameters {
        actual.setFloat(value, name)
        return this
    }

    public actual fun setDouble(name: String, value: Double): Parameters {
        actual.setDouble(value, name)
        return this
    }

    public actual fun setBoolean(name: String, value: Boolean): Parameters {
        actual.setBoolean(value, name)
        return this
    }

    public actual fun setDate(name: String, value: Instant?): Parameters {
        actual.setDate(value?.toNSDate(), name)
        return this
    }

    public actual fun setBlob(name: String, value: Blob?): Parameters {
        actual.setBlob(value?.actual, name)
        return this
    }

    public actual fun setDictionary(name: String, value: Dictionary?): Parameters {
        actual.setDictionary(value?.actual, name)
        return this
    }

    public actual fun setArray(name: String, value: Array?): Parameters {
        actual.setArray(value?.actual, name)
        return this
    }

    public actual fun setValue(name: String, value: Any?): Parameters {
        actual.setValue(value?.actualIfDelegated(), name)
        return this
    }
}

internal fun CBLQueryParameters.asParameters() = Parameters(this)
