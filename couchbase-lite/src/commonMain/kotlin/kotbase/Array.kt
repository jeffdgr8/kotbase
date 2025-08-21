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

import kotlin.time.Instant
import kotlin.reflect.safeCast

/**
 * Array provides readonly access to array data.
 */
public expect open class Array : ArrayInterface, Iterable<Any?> {

    internal val collectionMap: MutableMap<Int, Any>

    /**
     * Return a mutable copy of the array
     *
     * @return the MutableArray instance
     */
    public fun toMutable(): MutableArray

    override val count: Int

    override fun getValue(index: Int): Any?

    override fun getString(index: Int): String?

    override fun getNumber(index: Int): Number?

    override fun getInt(index: Int): Int

    override fun getLong(index: Int): Long

    override fun getFloat(index: Int): Float

    override fun getDouble(index: Int): Double

    override fun getBoolean(index: Int): Boolean

    override fun getBlob(index: Int): Blob?

    override fun getDate(index: Int): Instant?

    override fun getArray(index: Int): Array?

    override fun getDictionary(index: Int): Dictionary?

    override fun toList(): List<Any?>

    override fun toJSON(): String

    /**
     * An iterator over elements of this array.
     * A call to the `next()` method of the returned iterator
     * will throw a ConcurrentModificationException, if the MutableArray is
     * modified while it is in use.
     *
     * @return an iterator over the array's elements.
     */
    override fun iterator(): Iterator<Any?>
}

internal fun Array.checkIndex(index: Int) {
    if (index !in 0..<count) {
        throw IndexOutOfBoundsException("Array index $index is out of range")
    }
}

internal fun Array.checkInsertIndex(index: Int) {
    if (index !in 0..count) {
        throw IndexOutOfBoundsException("Array index $index is out of range")
    }
}

internal inline fun <reified T : Any> Array.getInternalCollection(index: Int): T? =
    T::class.safeCast(collectionMap[index])

@Suppress("UnusedReceiverParameter")
internal fun <T : Any> Array.incrementAfter(index: Int, collection: MutableMap<Int, T>) {
    for (key in collection.keys.sortedDescending()) {
        if (key >= index) {
            collection[key + 1] = collection.remove(key)!!
        } else {
            break
        }
    }
}
