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

import kotbase.internal.DbContext
import kotbase.internal.fleece.*
import kotlinx.cinterop.reinterpret
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual open class Dictionary
internal constructor(
    actual: FLDict,
    dbContext: DbContext?,
    release: Boolean = true
) : DictionaryInterface, Iterable<String> {

    private val memory = object {
        val actual = actual
        val release = release
    }

    internal open val actual: FLDict
        get() = memory.actual

    private val release: Boolean
        get() = memory.release

    internal open var dbContext: DbContext? = dbContext
        set(value) {
            field = value
            collectionMap.forEach {
                when (it) {
                    is Array -> it.dbContext = value
                    is Dictionary -> it.dbContext
                }
            }
        }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (it.release) FLDict_Release(it.actual)
    }

    internal actual val collectionMap: MutableMap<String, Any> = mutableMapOf()

    public actual fun toMutable(): MutableDictionary {
        return MutableDictionary(
            FLDict_MutableCopy(actual, kFLDeepCopy)!!,
            dbContext?.let { DbContext(it.database) }
        )
    }

    actual override val count: Int
        get() = FLDict_Count(actual).toInt()

    actual override val keys: List<String>
        get() = actual.keys()

    protected fun getFLValue(key: String): FLValue? =
        actual.getValue(key)

    actual override fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: getFLValue(key)?.toNative(dbContext, release)
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

    actual override fun getString(key: String): String? =
        getFLValue(key)?.toKString()

    actual override fun getNumber(key: String): Number? =
        getFLValue(key)?.toNumber()

    actual override fun getInt(key: String): Int =
        getFLValue(key).toInt()

    actual override fun getLong(key: String): Long =
        getFLValue(key).toLong()

    actual override fun getFloat(key: String): Float =
        getFLValue(key).toFloat()

    actual override fun getDouble(key: String): Double =
        getFLValue(key).toDouble()

    actual override fun getBoolean(key: String): Boolean =
        getFLValue(key).toBoolean()

    actual override fun getBlob(key: String): Blob? =
        getFLValue(key)?.toBlob(dbContext, release)

    actual override fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    actual override fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toArray(dbContext, release)
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toDictionary(dbContext, release)
                ?.also { collectionMap[key] = it }
    }

    actual override fun toMap(): Map<String, Any?> =
        actual.toMap(dbContext)

    actual override fun toJSON(): String =
        FLValue_ToJSON(actual.reinterpret()).toKString()!!

    actual override operator fun contains(key: String): Boolean =
        keys.contains(key)

    private var mutations: Long = 0

    protected fun mutate() {
        mutations++
    }

    private val isMutated: Boolean
        get() = mutations > 0

    actual override fun iterator(): Iterator<String> =
        DictionaryIterator(keys.iterator(), mutations)

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Dictionary) return false
        if (other.count != count) return false
        for (key in this) {
            val value = getValue(key)
            if (value != null) {
                if (value != other.getValue(key)) return false
            } else {
                if (!(other.getValue(key) == null && other.contains(key))) return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 0
        for (key in this) {
            val value = getValue(key)
            result += key.hashCode() xor (value?.hashCode() ?: 0)
        }
        return result
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
