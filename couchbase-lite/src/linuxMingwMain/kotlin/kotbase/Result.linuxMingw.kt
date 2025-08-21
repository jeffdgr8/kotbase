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

import cnames.structs.CBLQuery
import cnames.structs.CBLResultSet
import kotbase.internal.DbContext
import kotbase.internal.fleece.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlin.time.Instant
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Result
private constructor(
    query: CPointer<CBLQuery>,
    array: FLArray,
    dict: FLDict,
    private val dbContext: DbContext?
) : ArrayInterface, DictionaryInterface, Iterable<String> {

    internal constructor(rs: CPointer<CBLResultSet>, dbContext: DbContext?) : this(
        CBLResultSet_GetQuery(rs)!!,
        CBLResultSet_ResultArray(rs)!!,
        CBLResultSet_ResultDict(rs)!!,
        dbContext
    )

    private val memory = object {
        val query = query
        val array = array
        val dict = dict
    }

    init {
        CBLQuery_Retain(query)
        FLArray_Retain(array)
        FLDict_Retain(dict)
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLQuery_Release(it.query)
        FLArray_Release(it.array)
        FLDict_Release(it.dict)
    }

    private val query: CPointer<CBLQuery>
        get() = memory.query

    private val array: FLArray
        get() = memory.array

    private val dict: FLDict
        get() = memory.dict

    actual override val count: Int
        get() = CBLQuery_ColumnCount(query).toInt()

    private fun getFLValue(index: Int): FLValue? {
        assertInBounds(index)
        return FLArray_Get(array, index.convert())
    }

    actual override fun getValue(index: Int): Any? =
        getFLValue(index)?.toNative(dbContext)

    actual override fun getString(index: Int): String? =
        getFLValue(index)?.toKString()

    actual override fun getNumber(index: Int): Number? =
        getFLValue(index)?.toNumber()

    actual override fun getInt(index: Int): Int =
        getFLValue(index).toInt()

    actual override fun getLong(index: Int): Long =
        getFLValue(index).toLong()

    actual override fun getFloat(index: Int): Float =
        getFLValue(index).toFloat()

    actual override fun getDouble(index: Int): Double =
        getFLValue(index).toDouble()

    actual override fun getBoolean(index: Int): Boolean =
        getFLValue(index).toBoolean()

    actual override fun getBlob(index: Int): Blob? =
        getFLValue(index)?.toBlob(dbContext)

    actual override fun getDate(index: Int): Instant? =
        getFLValue(index)?.toDate()

    actual override fun getArray(index: Int): Array? =
        getFLValue(index)?.toArray(dbContext)

    actual override fun getDictionary(index: Int): Dictionary? =
        getFLValue(index)?.toDictionary(dbContext)

    actual override fun toList(): List<Any?> =
        array.toList(dbContext)

    actual override val keys: List<String>
        get() = buildList {
            repeat(count) { index ->
                add(CBLQuery_ColumnName(query, index.convert()).toKString()!!)
            }
        }

    private fun getFLValue(key: String): FLValue? {
        return memScoped {
            FLDict_Get(dict, key.toFLString(this))
        }
    }

    actual override fun getValue(key: String): Any? =
        getFLValue(key)?.toNative(dbContext)

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
        getFLValue(key)?.toBlob(dbContext)

    actual override fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    actual override fun getArray(key: String): Array? =
        getFLValue(key)?.toArray(dbContext)

    actual override fun getDictionary(key: String): Dictionary? =
        getFLValue(key)?.toDictionary(dbContext)

    actual override fun toMap(): Map<String, Any?> =
        dict.toMap(dbContext)

    actual override fun toJSON(): String =
        FLValue_ToJSON(dict.reinterpret()).toKString()!!

    actual override operator fun contains(key: String): Boolean =
        dict.getValue(key) != null

    actual override fun iterator(): Iterator<String> =
        keys.iterator()

    private fun isInBounds(index: Int): Boolean {
        return index in 0..<count
    }

    private fun assertInBounds(index: Int) {
        if (!isInBounds(index)) {
            throw IndexOutOfBoundsException("index $index must be between 0 and $count")
        }
    }
}
