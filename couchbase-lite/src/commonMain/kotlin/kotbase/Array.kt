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

import kotlinx.datetime.Instant
import kotlin.reflect.safeCast

/**
 * Array provides readonly access to array data.
 */
public expect open class Array : Iterable<Any?> {

    internal val collectionMap: MutableMap<Int, Any>

    /**
     * Return a mutable copy of the array
     *
     * @return the MutableArray instance
     */
    public fun toMutable(): MutableArray

    /**
     * The number of the items in the array.
     */
    public val count: Int

    /**
     * Gets value at the given index as an object. The object types are Blob,
     * Array, Dictionary, Number, or String based on the underlying
     * data type; or null if the value is null.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Object or null.
     */
    public fun getValue(index: Int): Any?

    /**
     * Gets value at the given index as a String. Returns null if the value doesn't exist, or its value is not a String.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the String or null.
     */
    public fun getString(index: Int): String?

    /**
     * Gets value at the given index as a Number. Returns null if the value doesn't exist, or its value is not a Number.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Number or nil.
     */
    public fun getNumber(index: Int): Number?

    /**
     * Gets value at the given index as an int.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the int value.
     */
    public fun getInt(index: Int): Int

    /**
     * Gets value at the given index as a long.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the long value.
     */
    public fun getLong(index: Int): Long

    /**
     * Gets value at the given index as a float.
     * Integers will be converted to float. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the value doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the float value.
     */
    public fun getFloat(index: Int): Float

    /**
     * Gets value at the given index as a double.
     * Integers will be converted to double. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the property doesn't exist or does not have a numeric value.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the double value.
     */
    public fun getDouble(index: Int): Double

    /**
     * Gets value at the given index as a boolean.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the boolean value.
     */
    public fun getBoolean(index: Int): Boolean

    /**
     * Gets value at the given index as a Blob.
     * Returns null if the value doesn't exist, or its value is not a Blob.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Blob value or null.
     */
    public fun getBlob(index: Int): Blob?

    /**
     * Gets value at the given index as a Date.
     * JSON does not directly support dates, so the actual property value must be a string, which is
     * then parsed according to the ISO-8601 date format (the default used in JSON.)
     * Returns null if the value doesn't exist, is not a string, or is not parsable as a date.
     * NOTE: This is not a generic date parser! It only recognizes the ISO-8601 format, with or
     * without milliseconds.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Date value or null.
     */
    public fun getDate(index: Int): Instant?

    /**
     * Gets value at the given index as an Array.
     * Returns null if the value doesn't exist, or its value is not an Array.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Array object.
     */
    public open fun getArray(index: Int): Array?

    /**
     * Gets a Dictionary at the given index. Return null if the value is not a dictionary.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Dictionary object.
     */
    public open fun getDictionary(index: Int): Dictionary?

    /**
     * Gets content of the current object as a List. The values contained in the returned
     * List object are all JSON based values.
     *
     * @return the List object representing the content of the current object in the JSON format.
     */
    public fun toList(): List<Any?>

    /**
     * Encode an Array as a JSON string
     *
     * @return JSON encoded representation of the Array
     * @throws CouchbaseLiteException on encoder failure.
     */
    public fun toJSON(): String

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

/**
 * Subscript access to a Fragment object by index.
 *
 * @param index The Index. If the index value exceeds the bounds of the array,
 * the MutableFragment object will represent a nil value.
 */
public operator fun Array.get(index: Int): Fragment {
    return if (index in 0..<count) {
        Fragment(this, index)
    } else {
        Fragment()
    }
}

internal fun Array.checkIndex(index: Int) {
    if (index < 0 || index >= count) {
        throw IndexOutOfBoundsException("Array index $index is out of range")
    }
}

internal fun Array.checkInsertIndex(index: Int) {
    if (index < 0 || index > count) {
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
