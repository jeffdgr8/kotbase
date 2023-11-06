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

/**
 * Result represents a row of result set returned by a Query.
 */
public expect class Result : Iterable<String> {

    /**
     * The number of the values in the result.
     */
    public val count: Int

    /**
     * The result value at the given index.
     *
     * @param index the index of the required value.
     * @return the value.
     */
    public fun getValue(index: Int): Any?

    /**
     * The result at the given index as a String
     *
     * @param index the index of the required value.
     * @return a String value.
     */
    public fun getString(index: Int): String?

    /**
     * The result  at the given index as a Number
     *
     * @param index the index of the required value.
     * @return a Number value.
     */
    public fun getNumber(index: Int): Number?

    /**
     * The result at the given index as an int
     *
     * @param index the index of the required value.
     * @return an int value.
     */
    public fun getInt(index: Int): Int

    /**
     * The result at the given index as a long
     *
     * @param index the index of the required value.
     * @return a long value.
     */
    public fun getLong(index: Int): Long

    /**
     * The result at the given index as a float
     *
     * @param index the index of the required value.
     * @return a float value.
     */
    public fun getFloat(index: Int): Float

    /**
     * The result at the given index as a double
     *
     * @param index the index of the required value.
     * @return a double value.
     */
    public fun getDouble(index: Int): Double

    /**
     * The result at the given index as a boolean
     *
     * @param index the index of the required value.
     * @return a boolean value.
     */
    public fun getBoolean(index: Int): Boolean

    /**
     * The result at the given index as a Blob
     *
     * @param index the index of the required value.
     * @return a Blob.
     */
    public fun getBlob(index: Int): Blob?

    /**
     * The result at the given index as a Date
     *
     * @param index the index of the required value.
     * @return a Date.
     */
    public fun getDate(index: Int): Instant?

    /**
     * The result at the given index as an Array
     *
     * @param index the index of the required value.
     * @return an Array.
     */
    public fun getArray(index: Int): Array?

    /**
     * The result at the given index as a Dictionary
     *
     * @param index the index of the required value.
     * @return a Dictionary.
     */
    public fun getDictionary(index: Int): Dictionary?

    /**
     * Gets all values as a List. The types of the values contained in the returned List
     * are Array, Blob, Dictionary, Number types, String, and null.
     *
     * @return a List containing all values.
     */
    public fun toList(): List<Any?>

    /**
     * A list of keys
     */
    public val keys: List<String>

    /**
     * The result value for the given key as an Object
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The Object.
     */
    public fun getValue(key: String): Any?

    /**
     * The result value for the given key as a String object
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The String object.
     */
    public fun getString(key: String): String?

    /**
     * The projecting result value for the given key  as a Number object
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The Number object.
     */
    public fun getNumber(key: String): Number?

    /**
     * The projecting result value for the given key  as an integer value
     * Returns 0 if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The integer value.
     */
    public fun getInt(key: String): Int

    /**
     * The projecting result value for the given key  as a long value
     * Returns 0L if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The long value.
     */
    public fun getLong(key: String): Long

    /**
     * The projecting result value for the given key  as a float value.
     * Returns 0.0f if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The float value.
     */
    public fun getFloat(key: String): Float

    /**
     * The projecting result value for the given key as a double value.
     * Returns 0.0 if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The double value.
     */
    public fun getDouble(key: String): Double

    /**
     * The projecting result value for the given key  as a boolean value.
     * Returns false if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The boolean value.
     */
    public fun getBoolean(key: String): Boolean

    /**
     * The projecting result value for the given key  as a Blob object.
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The Blob object.
     */
    public fun getBlob(key: String): Blob?

    /**
     * The projecting result value for the given key as a Date object.
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The Date object.
     */
    public fun getDate(key: String): Instant?

    /**
     * The projecting result value for the given key as a readonly Array object.
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The Array object.
     */
    public fun getArray(key: String): Array?

    /**
     * The projecting result value for the given key as a readonly Dictionary object.
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The Dictionary object.
     */
    public fun getDictionary(key: String): Dictionary?

    /**
     * Gets all values as a Dictionary. The value types of the values contained
     * in the returned Dictionary object are Array, Blob, Dictionary,
     * Number types, String, and null.
     *
     * @return The Map representing all values.
     */
    public fun toMap(): Map<String, Any?>

    public fun toJSON(): String

    /**
     * Tests whether a projecting result key exists or not.
     *
     * @param key The select result key.
     * @return True if exists, otherwise false.
     */
    public operator fun contains(key: String): Boolean

    /**
     * Gets  an iterator over the projecting result keys.
     *
     * @return The Iterator object of all result keys.
     */
    override operator fun iterator(): Iterator<String>
}

/**
 * Subscript access to a Fragment object of the projecting result
 * value at the given index.
 *
 * @param index The select result index.
 */
public operator fun Result.get(index: Int): Fragment {
    return if (index in 0 until count) {
        Fragment(this, index)
    } else {
        Fragment()
    }
}

/**
 * Subscript access to a Fragment object of the projecting result
 * value for the given key.
 *
 * @param key The select result key.
 */
public operator fun Result.get(key: String): Fragment =
    Fragment(this, key)
