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
 *
 * A Result may be referenced **only** while the ResultSet that contains it is open.
 * An Attempt to reference a Result after calling ResultSet.close on the ResultSet that
 * contains it will throw and IllegalStateException
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
     * The result at the given index converted to a String
     *
     * @param index the index of the required value.
     * @return a String value.
     */
    public fun getString(index: Int): String?

    /**
     * The result at the given index interpreted as a Number.
     * Returns null if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return a Number value.
     */
    public fun getNumber(index: Int): Number?

    /**
     * The result at the given index interpreted as and an int.
     * Returns 0 if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return an int value.
     */
    public fun getInt(index: Int): Int

    /**
     * The result at the given index interpreted as a long.
     * Returns 0 if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return a long value.
     */
    public fun getLong(index: Int): Long

    /**
     * The result at the given index interpreted as a float.
     * Returns 0.0F if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return a float value.
     */
    public fun getFloat(index: Int): Float

    /**
     * The result at the given index interpreted as a double.
     * Returns 0.0 if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return a double value.
     */
    public fun getDouble(index: Int): Double

    /**
     * The result at the given index interpreted as a boolean.
     * Returns false if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return a boolean value.
     */
    public fun getBoolean(index: Int): Boolean

    /**
     * The result at the given index interpreted as a Blob.
     * Returns null if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return a Blob.
     */
    public fun getBlob(index: Int): Blob?

    /**
     * The result at the given index interpreted as an Instant date.
     * Returns null if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return an Instant date.
     */
    public fun getDate(index: Int): Instant?

    /**
     * The result at the given index interpreted as an Array.
     * Returns null if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return an Array.
     */
    public fun getArray(index: Int): Array?

    /**
     * The result at the given index interpreted as a Dictionary.
     * Returns null if the value cannot be so interpreted.
     *
     * @param index the index of the required value.
     * @return a Dictionary.
     */
    public fun getDictionary(index: Int): Dictionary?

    /**
     * Gets all the values as a List. The types of the values contained in the returned List
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
     * The result value for the given key as a String
     * Returns null if the key doesn't exist.
     *
     * @param key The select result key.
     * @return The String object.
     */
    public fun getString(key: String): String?

    /**
     * The result value for the given key as a Number
     * Returns null if the key doesn't exist or if the value is not a Number
     *
     * @param key The select result key.
     * @return The Number object.
     */
    public fun getNumber(key: String): Number?

    /**
     * The result value for the given key as an int
     * Returns 0 if the key doesn't exist or if the value is not a int
     *
     * @param key The select result key.
     * @return The integer value.
     */
    public fun getInt(key: String): Int

    /**
     * The result value for the given key as a long
     * Returns 0L if the key doesn't exist or if the value is not a long
     *
     * @param key The select result key.
     * @return The long value.
     */
    public fun getLong(key: String): Long

    /**
     * The result value for the given key as a float
     * Returns 0.0F if the key doesn't exist or if the value is not a float
     *
     * @param key The select result key.
     * @return The float value.
     */
    public fun getFloat(key: String): Float

    /**
     * The result value for the given key as a double
     * Returns 0.0 if the key doesn't exist or if the value is not a double
     *
     * @param key The select result key.
     * @return The double value.
     */
    public fun getDouble(key: String): Double

    /**
     * The result value for the given key as a boolean
     * Returns false if the key doesn't exist or if the value is not a boolean
     *
     * @param key The select result key.
     * @return The boolean value.
     */
    public fun getBoolean(key: String): Boolean

    /**
     * The result value for the given key as a Blob
     * Returns null if the key doesn't exist or if the value is not a Blob
     *
     * @param key The select result key.
     * @return The Blob object.
     */
    public fun getBlob(key: String): Blob?

    /**
     * The result value for the given key as an Instant date
     * Returns null if the key doesn't exist or if the value is not a date
     *
     * @param key The select result key.
     * @return The Instant date object.
     */
    public fun getDate(key: String): Instant?

    /**
     * The result value for the given key as a Array
     * Returns null if the key doesn't exist or if the value is not an Array
     *
     * @param key The select result key.
     * @return The Array object.
     */
    public fun getArray(key: String): Array?

    /**
     * The result value for the given key as a Dictionary
     * Returns null if the key doesn't exist or if the value is not a Dictionary
     *
     * @param key The select result key.
     * @return The Dictionary object.
     */
    public fun getDictionary(key: String): Dictionary?

    /**
     * Gets all values as a Map. The keys in the returned map are the names of columns that have
     * values. The types of the values are Array, Blob, Dictionary, Number types, String, and null.
     *
     * @return The Map representing all values.
     */
    public fun toMap(): Map<String, Any?>

    public fun toJSON(): String

    /**
     * Tests whether key exists or not.
     *
     * @param key The select result key.
     * @return True if exists, otherwise false.
     */
    public operator fun contains(key: String): Boolean

    /**
     * Gets an iterator over the result's keys.
     *
     * @return The Iterator object of all result keys.
     */
    override fun iterator(): Iterator<String>
}

/**
 * Subscript access to a Fragment object of the projecting result
 * value at the given index.
 *
 * @param index The select result index.
 */
public operator fun Result.get(index: Int): Fragment {
    return if (index in 0..<count) {
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
