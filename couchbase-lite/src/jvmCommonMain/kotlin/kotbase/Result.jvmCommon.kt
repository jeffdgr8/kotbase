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

import kotbase.internal.DelegatedClass
import kotbase.ext.toKotlinInstant
import kotlin.time.Instant
import com.couchbase.lite.Result as CBLResult

public actual class Result
internal constructor(actual: CBLResult) : DelegatedClass<CBLResult>(actual), ArrayInterface, DictionaryInterface, Iterable<String> {

    actual override val count: Int
        get() = actual.count()

    actual override fun getValue(index: Int): Any? =
        actual.getValue(index)?.delegateIfNecessary()

    actual override fun getString(index: Int): String? =
        actual.getString(index)

    actual override fun getNumber(index: Int): Number? =
        actual.getNumber(index)

    actual override fun getInt(index: Int): Int =
        actual.getInt(index)

    actual override fun getLong(index: Int): Long =
        actual.getLong(index)

    actual override fun getFloat(index: Int): Float =
        actual.getFloat(index)

    actual override fun getDouble(index: Int): Double =
        actual.getDouble(index)

    actual override fun getBoolean(index: Int): Boolean =
        actual.getBoolean(index)

    actual override fun getBlob(index: Int): Blob? =
        actual.getBlob(index)?.asBlob()

    actual override fun getDate(index: Int): Instant? =
        actual.getDate(index)?.toKotlinInstant()

    actual override fun getArray(index: Int): Array? =
        actual.getArray(index)?.asArray()

    actual override fun getDictionary(index: Int): Dictionary? =
        actual.getDictionary(index)?.asDictionary()

    actual override fun toList(): List<Any?> =
        actual.toList().delegateIfNecessary()

    actual override val keys: List<String>
        get() = actual.keys

    actual override fun getValue(key: String): Any? =
        actual.getValue(key)?.delegateIfNecessary()

    actual override fun getString(key: String): String? =
        actual.getString(key)

    actual override fun getNumber(key: String): Number? =
        actual.getNumber(key)

    actual override fun getInt(key: String): Int =
        actual.getInt(key)

    actual override fun getLong(key: String): Long =
        actual.getLong(key)

    actual override fun getFloat(key: String): Float =
        actual.getFloat(key)

    actual override fun getDouble(key: String): Double =
        actual.getDouble(key)

    actual override fun getBoolean(key: String): Boolean =
        actual.getBoolean(key)

    actual override fun getBlob(key: String): Blob? =
        actual.getBlob(key)?.asBlob()

    actual override fun getDate(key: String): Instant? =
        actual.getDate(key)?.toKotlinInstant()

    actual override fun getArray(key: String): Array? =
        actual.getArray(key)?.asArray()

    actual override fun getDictionary(key: String): Dictionary? =
        actual.getDictionary(key)?.asDictionary()

    actual override fun toMap(): Map<String, Any?> =
        actual.toMap().delegateIfNecessary()

    actual override fun toJSON(): String =
        actual.toJSON()

    actual override operator fun contains(key: String): Boolean =
        actual.contains(key)

    actual override fun iterator(): Iterator<String> =
        actual.iterator()
}

internal fun CBLResult.asResult() = Result(this)
