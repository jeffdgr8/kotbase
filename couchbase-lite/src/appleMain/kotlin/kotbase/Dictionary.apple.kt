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

import cocoapods.CouchbaseLite.CBLDictionary
import kotbase.ext.asNumber
import kotbase.internal.DelegatedClass
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

public actual open class Dictionary
internal constructor(actual: CBLDictionary) : DelegatedClass<CBLDictionary>(actual), Iterable<String> {

    internal actual val collectionMap: MutableMap<String, Any> = mutableMapOf()

    public actual fun toMutable(): MutableDictionary =
        MutableDictionary(actual.toMutable())

    public actual val count: Int
        get() = actual.count.toInt()

    @Suppress("UNCHECKED_CAST")
    public actual val keys: List<String>
        get() = actual.keys as List<String>

    public actual open fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: actual.valueForKey(key)?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

    public actual fun getString(key: String): String? =
        actual.stringForKey(key)

    public actual fun getNumber(key: String): Number? =
        actual.numberForKey(key)?.asNumber()

    public actual fun getInt(key: String): Int =
        actual.integerForKey(key).toInt()

    public actual fun getLong(key: String): Long =
        actual.longLongForKey(key)

    public actual fun getFloat(key: String): Float =
        actual.floatForKey(key)

    public actual fun getDouble(key: String): Double =
        actual.doubleForKey(key)

    public actual fun getBoolean(key: String): Boolean =
        actual.booleanForKey(key)

    public actual fun getBlob(key: String): Blob? =
        actual.blobForKey(key)?.asBlob()

    public actual fun getDate(key: String): Instant? =
        actual.dateForKey(key)?.toKotlinInstant()

    public actual open fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: actual.arrayForKey(key)?.asArray()
                ?.also { collectionMap[key] = it }
    }

    public actual open fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: actual.dictionaryForKey(key)?.asDictionary()
                ?.also { collectionMap[key] = it }
    }

    @Suppress("UNCHECKED_CAST")
    public actual fun toMap(): Map<String, Any?> =
        actual.toDictionary().delegateIfNecessary() as Map<String, Any?>

    public actual open fun toJSON(): String =
        actual.toJSON()

    public actual operator fun contains(key: String): Boolean =
        actual.containsValueForKey(key)

    @Suppress("UNCHECKED_CAST")
    override operator fun iterator(): Iterator<String> =
        (actual.keys as List<String>).iterator()
}

internal fun CBLDictionary.asDictionary() = Dictionary(this)
