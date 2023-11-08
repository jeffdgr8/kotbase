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
    dbContext: DbContext?
) : Iterable<String> {

    init {
        FLDict_Retain(actual)
    }

    internal open val actual: FLDict = actual

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
    private val cleaner = createCleaner(actual) {
        FLDict_Release(it)
    }

    internal actual val collectionMap: MutableMap<String, Any> = mutableMapOf()

    public actual fun toMutable(): MutableDictionary {
        return MutableDictionary(
            FLDict_MutableCopy(actual, kFLDeepCopy)!!,
            dbContext?.let { DbContext(it.database) }
        )
    }

    public actual val count: Int
        get() = FLDict_Count(actual).toInt()

    public actual val keys: List<String>
        get() = actual.keys()

    protected fun getFLValue(key: String): FLValue? =
        actual.getValue(key)

    public actual open fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: getFLValue(key)?.toNative(dbContext)
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

    public actual fun getString(key: String): String? =
        getFLValue(key)?.toKString()

    public actual fun getNumber(key: String): Number? =
        getFLValue(key)?.toNumber()

    public actual fun getInt(key: String): Int =
        getFLValue(key).toInt()

    public actual fun getLong(key: String): Long =
        getFLValue(key).toLong()

    public actual fun getFloat(key: String): Float =
        getFLValue(key).toFloat()

    public actual fun getDouble(key: String): Double =
        getFLValue(key).toDouble()

    public actual fun getBoolean(key: String): Boolean =
        getFLValue(key).toBoolean()

    public actual open fun getBlob(key: String): Blob? =
        getFLValue(key)?.toBlob(dbContext)

    public actual fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    public actual open fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toArray(dbContext)
                ?.also { collectionMap[key] = it }
    }

    public actual open fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toDictionary(dbContext)
                ?.also { collectionMap[key] = it }
    }

    public actual fun toMap(): Map<String, Any?> =
        actual.toMap(dbContext)

    public actual open fun toJSON(): String =
        FLValue_ToJSON(actual.reinterpret()).toKString()!!

    public actual operator fun contains(key: String): Boolean =
        keys.contains(key)

    override fun iterator(): Iterator<String> =
        keys.iterator()

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
            //append(if (isMutated) '!' else '.')
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
