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

import cocoapods.CouchbaseLite.CBLIndexUpdater
import kotbase.ext.asNumber
import kotbase.ext.wrapCBLError
import kotbase.internal.DelegatedClass
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlin.reflect.safeCast

internal class IndexUpdaterImpl(actual: CBLIndexUpdater) : DelegatedClass<CBLIndexUpdater>(actual), IndexUpdater {

    private val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    override val count: Int
        get() = actual.count.toInt()

    override fun getValue(index: Int): Any? {
        checkIndex(index)
        return collectionMap[index]
            ?: actual.valueAtIndex(index.convert())?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    override fun getString(index: Int): String? {
        checkIndex(index)
        return actual.stringAtIndex(index.convert())
    }

    override fun getNumber(index: Int): Number? {
        checkIndex(index)
        return actual.numberAtIndex(index.convert())?.asNumber()
    }

    override fun getInt(index: Int): Int {
        checkIndex(index)
        return actual.integerAtIndex(index.convert()).toInt()
    }

    override fun getLong(index: Int): Long {
        checkIndex(index)
        return actual.longLongAtIndex(index.convert())
    }

    override fun getFloat(index: Int): Float {
        checkIndex(index)
        return actual.floatAtIndex(index.convert())
    }

    override fun getDouble(index: Int): Double {
        checkIndex(index)
        return actual.doubleAtIndex(index.convert())
    }

    override fun getBoolean(index: Int): Boolean {
        checkIndex(index)
        return actual.booleanAtIndex(index.convert())
    }

    override fun getBlob(index: Int): Blob? {
        checkIndex(index)
        return actual.blobAtIndex(index.convert())?.asBlob()
    }

    override fun getDate(index: Int): Instant? {
        checkIndex(index)
        return actual.dateAtIndex(index.convert())?.toKotlinInstant()
    }

    override fun getArray(index: Int): Array? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.arrayAtIndex(index.convert())?.asArray()
                ?.also { collectionMap[index] = it }
    }

    override fun getDictionary(index: Int): Dictionary? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.dictionaryAtIndex(index.convert())?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    override fun toList(): List<Any?> =
        actual.toArray().delegateIfNecessary()

    override fun toJSON(): String =
        actual.toJSON()

    override fun iterator(): Iterator<Any?> =
        IndexUpdaterIterator(count)

    private inner class IndexUpdaterIterator(private val count: Int) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean = index < count

        override fun next(): Any? =
            getValue(index++)
    }

    override fun setVector(value: List<Float>?, index: Int) {
        wrapCBLError { error ->
            actual.setVector(value, index.convert(), error)
        }
    }

    override fun skipVector(index: Int) {
        actual.skipVectorAtIndex(index.convert())
    }

    override fun finish() {
        wrapCBLError { error ->
            actual.finishWithError(error)
        }
    }

    override fun close() {
        wrapCBLError { error ->
            actual.finishWithError(error)
        }
    }

    private fun checkIndex(index: Int) {
        if (index < 0 || index >= count) {
            throw IndexOutOfBoundsException("Array index $index is out of range")
        }
    }

    private inline fun <reified T : Any> getInternalCollection(index: Int): T? =
        T::class.safeCast(collectionMap[index])
}

internal fun CBLIndexUpdater.asIndexUpdater(): IndexUpdater = IndexUpdaterImpl(this)
