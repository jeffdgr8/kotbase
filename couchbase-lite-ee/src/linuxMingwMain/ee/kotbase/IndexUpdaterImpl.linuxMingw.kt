/*
 * Copyright 2024 Jeff Lockhart
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

import cnames.structs.CBLIndexUpdater
import kotbase.internal.DbContext
import kotbase.internal.fleece.toArray
import kotbase.internal.fleece.toBlob
import kotbase.internal.fleece.toBoolean
import kotbase.internal.fleece.toDate
import kotbase.internal.fleece.toDictionary
import kotbase.internal.fleece.toDouble
import kotbase.internal.fleece.toFloat
import kotbase.internal.fleece.toInt
import kotbase.internal.fleece.toKString
import kotbase.internal.fleece.toLong
import kotbase.internal.fleece.toNative
import kotbase.internal.fleece.toNumber
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCValues
import libcblite.CBLIndexUpdater_Count
import libcblite.CBLIndexUpdater_Finish
import libcblite.CBLIndexUpdater_Release
import libcblite.CBLIndexUpdater_SetVector
import libcblite.CBLIndexUpdater_SkipVector
import libcblite.CBLIndexUpdater_Value
import libcblite.FLValue
import libcblite.FLValue_ToJSON
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner
import kotlin.reflect.safeCast
import kotlin.time.Instant

internal class IndexUpdaterImpl(
    internal val actual: CPointer<CBLIndexUpdater>,
    private val dbContext: DbContext?
) : IndexUpdater {

    init {
        debug.RefTracker.trackInit(actual, "CBLIndexUpdater")
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        debug.CBLIndexUpdater_Release(it)
    }

    private val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    override val count: Int
        get() {
            checkIsFinished()
            return CBLIndexUpdater_Count(actual).toInt()
        }

    private fun getFLValue(index: Int): FLValue? {
        checkIsFinished()
        checkIndex(index)
        return CBLIndexUpdater_Value(actual, index.convert())
    }

    override fun getValue(index: Int): Any? {
        checkIsFinished()
        return collectionMap[index]
            ?: getFLValue(index)?.toNative(dbContext, true)
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    override fun getString(index: Int): String? =
        getFLValue(index)?.toKString()

    override fun getNumber(index: Int): Number? =
        getFLValue(index)?.toNumber()

    override fun getInt(index: Int): Int =
        getFLValue(index).toInt()

    override fun getLong(index: Int): Long =
        getFLValue(index).toLong()

    override fun getFloat(index: Int): Float =
        getFLValue(index).toFloat()

    override fun getDouble(index: Int): Double =
        getFLValue(index).toDouble()

    override fun getBoolean(index: Int): Boolean =
        getFLValue(index).toBoolean()

    override fun getBlob(index: Int): Blob? =
        getFLValue(index)?.toBlob(dbContext, true)

    override fun getDate(index: Int): Instant? =
        getFLValue(index)?.toDate()

    override fun getArray(index: Int): Array? {
        checkIsFinished()
        return getInternalCollection(index)
            ?: getFLValue(index)?.toArray(dbContext, true)
                ?.also { collectionMap[index] = it }
    }

    override fun getDictionary(index: Int): Dictionary? {
        checkIsFinished()
        return getInternalCollection(index)
            ?: getFLValue(index)?.toDictionary(dbContext, true)
                ?.also { collectionMap[index] = it }
    }

    override fun toList(): List<Any?> {
        checkIsFinished()
        return iterator().asSequence().toList().map {
            // TODO: remove when this bug is fixed
            //  https://www.couchbase.com/forums/t/indexupdater-tolist-doesnt-do-a-deep-conversion-in-java-sdk/40990
            when (it) {
                is Array -> it.toList()
                is Dictionary -> it.toMap()
                else -> it
            }
        }
    }

    override fun toJSON(): String {
        checkIsFinished()
        return debug.FLValue_ToJSON(actual.reinterpret()).toKString()!!
    }

    override fun iterator(): Iterator<Any?> =
        IndexUpdaterIterator(count)

    private inner class IndexUpdaterIterator(private val count: Int) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean = index < count

        override fun next(): Any? =
            getValue(index++)
    }

    override fun setVector(value: List<Float>?, index: Int) {
        checkIsFinished()
        checkIndex(index)
        wrapCBLError { error ->
            CBLIndexUpdater_SetVector(
                actual,
                index.convert(),
                value?.toFloatArray()?.toCValues(),
                (value?.size ?: 0).convert(),
                error
            )
        }
    }

    override fun skipVector(index: Int) {
        checkIsFinished()
        checkIndex(index)
        CBLIndexUpdater_SkipVector(actual, index.convert())
    }

    override fun finish() {
        checkIsFinished()
        wrapCBLError { error ->
            CBLIndexUpdater_Finish(actual, error)
        }
        finished = true
    }

    override fun close() {
        finish()
    }

    private var finished = false

    private fun checkIsFinished() {
        if (finished) throw CouchbaseLiteError("Called on finished updater")
    }

    private fun checkIndex(index: Int) {
        if (index !in 0..<count) {
            throw IndexOutOfBoundsException("Array index $index is out of range")
        }
    }

    private inline fun <reified T : Any> getInternalCollection(index: Int): T? =
        T::class.safeCast(collectionMap[index])
}

internal fun CPointer<CBLIndexUpdater>.asIndexUpdater(dbContext: DbContext?): IndexUpdater =
    IndexUpdaterImpl(this, dbContext)
