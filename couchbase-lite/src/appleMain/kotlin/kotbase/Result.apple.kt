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

import cocoapods.CouchbaseLite.CBLQueryResult
import kotbase.internal.DelegatedClass
import kotbase.ext.asNumber
import kotbase.ext.toKotlinInstantMillis
import kotlinx.cinterop.convert
import kotlin.time.Instant

public actual class Result
internal constructor(actual: CBLQueryResult) : DelegatedClass<CBLQueryResult>(actual), ArrayInterface, DictionaryInterface, Iterable<String> {

    actual override val count: Int
        get() = actual.count().toInt()

    actual override fun getValue(index: Int): Any? {
        assertInBounds(index)
        return actual.valueAtIndex(index.convert())?.delegateIfNecessary()
    }

    actual override fun getString(index: Int): String? {
        assertInBounds(index)
        return actual.stringAtIndex(index.convert())
    }

    actual override fun getNumber(index: Int): Number? {
        assertInBounds(index)
        return actual.numberAtIndex(index.convert())?.asNumber()
    }

    actual override fun getInt(index: Int): Int {
        assertInBounds(index)
        return actual.integerAtIndex(index.convert()).toInt()
    }

    actual override fun getLong(index: Int): Long {
        assertInBounds(index)
        return actual.longLongAtIndex(index.convert())
    }

    actual override fun getFloat(index: Int): Float {
        assertInBounds(index)
        return actual.floatAtIndex(index.convert())
    }

    actual override fun getDouble(index: Int): Double {
        assertInBounds(index)
        return actual.doubleAtIndex(index.convert())
    }

    actual override fun getBoolean(index: Int): Boolean {
        assertInBounds(index)
        return actual.booleanAtIndex(index.convert())
    }

    actual override fun getBlob(index: Int): Blob? {
        assertInBounds(index)
        return actual.blobAtIndex(index.convert())?.asBlob()
    }

    actual override fun getDate(index: Int): Instant? {
        assertInBounds(index)
        return actual.dateAtIndex(index.convert())?.toKotlinInstantMillis()
    }

    actual override fun getArray(index: Int): Array? {
        assertInBounds(index)
        return actual.arrayAtIndex(index.convert())?.asArray()
    }

    actual override fun getDictionary(index: Int): Dictionary? {
        assertInBounds(index)
        return actual.dictionaryAtIndex(index.convert())?.asDictionary()
    }

    actual override fun toList(): List<Any?> =
        actual.toArray().delegateIfNecessary()

    @Suppress("UNCHECKED_CAST")
    actual override val keys: List<String>
        get() = actual.keys as List<String>

    actual override fun getValue(key: String): Any? =
        actual.valueForKey(key)?.delegateIfNecessary()

    actual override fun getString(key: String): String? =
        actual.stringForKey(key)

    actual override fun getNumber(key: String): Number? =
        actual.numberForKey(key)?.asNumber()

    actual override fun getInt(key: String): Int =
        actual.integerForKey(key).toInt()

    actual override fun getLong(key: String): Long =
        actual.longLongForKey(key)

    actual override fun getFloat(key: String): Float =
        actual.floatForKey(key)

    actual override fun getDouble(key: String): Double =
        actual.doubleForKey(key)

    actual override fun getBoolean(key: String): Boolean =
        actual.booleanForKey(key)

    actual override fun getBlob(key: String): Blob? =
        actual.blobForKey(key)?.asBlob()

    actual override fun getDate(key: String): Instant? =
        actual.dateForKey(key)?.toKotlinInstantMillis()

    actual override fun getArray(key: String): Array? =
        actual.arrayForKey(key)?.asArray()

    actual override fun getDictionary(key: String): Dictionary? =
        actual.dictionaryForKey(key)?.asDictionary()

    @Suppress("UNCHECKED_CAST")
    actual override fun toMap(): Map<String, Any?> =
        actual.toDictionary().delegateIfNecessary() as Map<String, Any?>

    actual override fun toJSON(): String =
        actual.toJSON()

    actual override operator fun contains(key: String): Boolean =
        actual.containsValueForKey(key)

    @Suppress("UNCHECKED_CAST")
    actual override fun iterator(): Iterator<String> =
        (actual.keys as List<String>).iterator()

    private fun isInBounds(index: Int): Boolean {
        return index in 0..<count
    }

    private fun assertInBounds(index: Int) {
        if (!isInBounds(index)) {
            throw IndexOutOfBoundsException("index $index must be between 0 and $count")
        }
    }
}

internal fun CBLQueryResult.asResult() = Result(this)
