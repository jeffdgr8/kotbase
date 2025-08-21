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
import kotbase.ext.toKotlinInstantMillis
import kotbase.internal.DelegatedClass
import kotlin.time.Instant

public actual open class Dictionary
internal constructor(actual: CBLDictionary) : DelegatedClass<CBLDictionary>(actual), DictionaryInterface, Iterable<String> {

    internal actual val collectionMap: MutableMap<String, Any> = mutableMapOf()

    public actual fun toMutable(): MutableDictionary =
        MutableDictionary(actual.toMutable())

    actual override val count: Int
        get() = actual.count.toInt()

    @Suppress("UNCHECKED_CAST")
    actual override val keys: List<String>
        get() = actual.keys as List<String>

    actual override fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: actual.valueForKey(key)?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

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

    actual override fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: actual.arrayForKey(key)?.asArray()
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: actual.dictionaryForKey(key)?.asDictionary()
                ?.also { collectionMap[key] = it }
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun toMap(): Map<String, Any?> =
        actual.toDictionary().delegateIfNecessary() as Map<String, Any?>

    actual override fun toJSON(): String =
        actual.toJSON()

    actual override operator fun contains(key: String): Boolean =
        actual.containsValueForKey(key)

    private var mutations: Long = 0

    protected fun mutate() {
        mutations++
    }

    private val isMutated: Boolean
        get() = mutations > 0

    @Suppress("UNCHECKED_CAST")
    actual override fun iterator(): Iterator<String> =
        DictionaryIterator((actual.keys as List<String>).iterator(), mutations)

    private inner class DictionaryIterator(
        private val iterator: Iterator<String>,
        private val mutations: Long
    ) : Iterator<String> {

        override fun hasNext(): Boolean = iterator.hasNext()

        override fun next(): String {
            if (this@Dictionary.mutations != mutations) {
                throw ConcurrentModificationException("Dictionary modified during iteration")
            }
            return iterator.next()
        }
    }

    override fun toString(): String {
        return buildString {
            append("Dictionary{(")
            append(if (this@Dictionary is MutableDictionary) '+' else '.')
            append(if (isMutated) '!' else '.')
            append(')')
            var first = true
            for (key in keys) {
                if (first) {
                    first = false
                } else {
                    append(',')
                }
                append(key).append("=>").append(getValue(key))
            }
            append('}')
        }
    }
}

internal fun CBLDictionary.asDictionary() = Dictionary(this)
