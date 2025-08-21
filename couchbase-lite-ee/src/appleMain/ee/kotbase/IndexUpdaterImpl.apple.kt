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
import kotlinx.datetime.toKotlinInstant
import kotlin.reflect.safeCast
import kotlin.time.Instant

internal class IndexUpdaterImpl(actual: CBLIndexUpdater) : DelegatedClass<CBLIndexUpdater>(actual), IndexUpdater {

    private val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    override val count: Int
        get() {
            checkIsFinished()
            return actual.count.toInt()
        }

    override fun getValue(index: Int): Any? {
        checkIsFinished()
        checkIndex(index)
        return collectionMap[index]
            ?: actual.valueAtIndex(index.convert())?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    override fun getString(index: Int): String? {
        checkIsFinished()
        checkIndex(index)
        return actual.stringAtIndex(index.convert())
    }

    override fun getNumber(index: Int): Number? {
        checkIsFinished()
        checkIndex(index)
        return actual.numberAtIndex(index.convert())?.asNumber()
    }

    override fun getInt(index: Int): Int {
        checkIsFinished()
        checkIndex(index)
        return actual.integerAtIndex(index.convert()).toInt()
    }

    override fun getLong(index: Int): Long {
        checkIsFinished()
        checkIndex(index)
        return actual.longLongAtIndex(index.convert())
    }

    override fun getFloat(index: Int): Float {
        checkIsFinished()
        checkIndex(index)
        return actual.floatAtIndex(index.convert())
    }

    override fun getDouble(index: Int): Double {
        checkIsFinished()
        checkIndex(index)
        return actual.doubleAtIndex(index.convert())
    }

    override fun getBoolean(index: Int): Boolean {
        checkIsFinished()
        checkIndex(index)
        return actual.booleanAtIndex(index.convert())
    }

    override fun getBlob(index: Int): Blob? {
        checkIsFinished()
        checkIndex(index)
        return actual.blobAtIndex(index.convert())?.asBlob()
    }

    override fun getDate(index: Int): Instant? {
        checkIsFinished()
        checkIndex(index)
        return actual.dateAtIndex(index.convert())?.toKotlinInstant()
    }

    override fun getArray(index: Int): Array? {
        checkIsFinished()
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.arrayAtIndex(index.convert())?.asArray()
                ?.also { collectionMap[index] = it }
    }

    override fun getDictionary(index: Int): Dictionary? {
        checkIsFinished()
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.dictionaryAtIndex(index.convert())?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    override fun toList(): List<Any?> {
        checkIsFinished()
        return actual.toArray().delegateIfNecessary()
    }

    override fun toJSON(): String {
        checkIsFinished()
        return actual.toJSON()
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
            actual.setVector(value, index.convert(), error)
        }
    }

    override fun skipVector(index: Int) {
        checkIsFinished()
        checkIndex(index)
        actual.skipVectorAtIndex(index.convert())
    }

    override fun finish() {
        checkIsFinished()
        wrapCBLError { error ->
            actual.finishWithError(error)
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

internal fun CBLIndexUpdater.asIndexUpdater(): IndexUpdater = IndexUpdaterImpl(this)
