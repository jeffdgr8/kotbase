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
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Result
private constructor(
    query: CPointer<CBLQuery>,
    array: FLArray,
    dict: FLDict,
    private val dbContext: DbContext?
) : Iterable<String> {

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

    public actual val count: Int
        get() = CBLQuery_ColumnCount(query).toInt()

    private fun getFLValue(index: Int): FLValue? {
        assertInBounds(index)
        return FLArray_Get(array, index.convert())
    }

    public actual fun getValue(index: Int): Any? =
        getFLValue(index)?.toNative(dbContext)

    public actual fun getString(index: Int): String? =
        getFLValue(index)?.toKString()

    public actual fun getNumber(index: Int): Number? =
        getFLValue(index)?.toNumber()

    public actual fun getInt(index: Int): Int =
        getFLValue(index).toInt()

    public actual fun getLong(index: Int): Long =
        getFLValue(index).toLong()

    public actual fun getFloat(index: Int): Float =
        getFLValue(index).toFloat()

    public actual fun getDouble(index: Int): Double =
        getFLValue(index).toDouble()

    public actual fun getBoolean(index: Int): Boolean =
        getFLValue(index).toBoolean()

    public actual fun getBlob(index: Int): Blob? =
        getFLValue(index)?.toBlob(dbContext)

    public actual fun getDate(index: Int): Instant? =
        getFLValue(index)?.toDate()

    public actual fun getArray(index: Int): Array? =
        getFLValue(index)?.toArray(dbContext)

    public actual fun getDictionary(index: Int): Dictionary? =
        getFLValue(index)?.toDictionary(dbContext)

    public actual fun toList(): List<Any?> =
        array.toList(dbContext)

    public actual val keys: List<String>
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

    public actual fun getValue(key: String): Any? =
        getFLValue(key)?.toNative(dbContext)

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

    public actual fun getBlob(key: String): Blob? =
        getFLValue(key)?.toBlob(dbContext)

    public actual fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    public actual fun getArray(key: String): Array? =
        getFLValue(key)?.toArray(dbContext)

    public actual fun getDictionary(key: String): Dictionary? =
        getFLValue(key)?.toDictionary(dbContext)

    public actual fun toMap(): Map<String, Any?> =
        dict.toMap(dbContext)

    public actual fun toJSON(): String =
        FLValue_ToJSON(dict.reinterpret()).toKString()!!

    public actual operator fun contains(key: String): Boolean =
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
