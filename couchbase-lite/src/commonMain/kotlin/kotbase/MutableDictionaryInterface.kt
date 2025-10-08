/*
 * Copyright 2025 Jeff Lockhart
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
 * MutableDictionaryInterface defines a set of methods for getting and setting dictionary data.
 */
public interface MutableDictionaryInterface : DictionaryInterface {

    /**
     * Get a property's value as an Array.
     * Returns null if the property doesn't exist, or its value is not an array.
     *
     * @param key the key.
     * @return the Array object.
     */
    override fun getArray(key: String): MutableArray?

    /**
     * Get a property's value as a Dictionary.
     * Returns null if the property doesn't exist, or its value is not a dictionary.
     *
     * @param key the key.
     * @return the Dictionary object or null if the key doesn't exist.
     */
    override fun getDictionary(key: String): MutableDictionary?

    // remove

    /**
     * Removes the mapping for a key from this Dictionary
     *
     * @param key the key.
     * @return The self object.
     */
    public fun remove(key: String): MutableDictionaryInterface

    // set

    /**
     * Set an int value for the given key.
     *
     * @param key   The key
     * @param value The int value.
     * @return The self object.
     */
    public fun setInt(key: String, value: Int): MutableDictionaryInterface

    /**
     * Set a long value for the given key.
     *
     * @param key   The key
     * @param value The long value.
     * @return The self object.
     */
    public fun setLong(key: String, value: Long): MutableDictionaryInterface

    /**
     * Set a float value for the given key.
     *
     * @param key   The key
     * @param value The float value.
     * @return The self object.
     */
    public fun setFloat(key: String, value: Float): MutableDictionaryInterface

    /**
     * Set a double value for the given key.
     *
     * @param key   The key
     * @param value The double value.
     * @return The self object.
     */
    public fun setDouble(key: String, value: Double): MutableDictionaryInterface

    /**
     * Set a boolean value for the given key.
     *
     * @param key   The key
     * @param value The boolean value.
     * @return The self object.
     */
    public fun setBoolean(key: String, value: Boolean): MutableDictionaryInterface

    /**
     * Set a Number value for the given key.
     *
     * @param key   The key
     * @param value The number value.
     * @return The self object.
     */
    public fun setNumber(key: String, value: Number?): MutableDictionaryInterface

    /**
     * Set a String value for the given key.
     *
     * @param key   The key
     * @param value The String value.
     * @return The self object.
     */
    public fun setString(key: String, value: String?): MutableDictionaryInterface

    /**
     * Set an Instant date object for the given key.
     *
     * @param key   The key
     * @param value The Instant date object.
     * @return The self object.
     */
    public fun setDate(key: String, value: Instant?): MutableDictionaryInterface

    /**
     * Set a Blob object for the given key.
     *
     * @param key   The key
     * @param value The Blob object.
     * @return The self object.
     */
    public fun setBlob(key: String, value: Blob?): MutableDictionaryInterface

    /**
     * Set an object value by key.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
     *
     * @param key   the key.
     * @param value the object value.
     * @return The self object.
     */
    public fun setValue(key: String, value: Any?): MutableDictionaryInterface

    /**
     * Set an Array object for the given key.
     *
     * @param key   The key
     * @param value The Array object.
     * @return The self object.
     */
    public fun setArray(key: String, value: Array?): MutableDictionaryInterface

    /**
     * Set a Dictionary object for the given key.
     *
     * @param key   The key
     * @param value The Dictionary object.
     * @return The self object.
     */
    public fun setDictionary(key: String, value: Dictionary?): MutableDictionaryInterface

    /**
     * Populate a dictionary with content from a Map.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
     * Setting the dictionary content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param data the dictionary object.
     * @return The self object.
     */
    public fun setData(data: Map<String, Any?>): MutableDictionaryInterface

    /**
     * Populate a dictionary with content from a JSON string.
     * Setting the dictionary content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param json the dictionary object.
     * @return this Document instance
     */
    public fun setJSON(json: String): MutableDictionaryInterface
}

/**
 * Subscript access to a MutableFragment object of the projecting result
 * value for the given key.
 *
 * @param key The select result key.
 */
public operator fun MutableDictionaryInterface.get(key: String): MutableFragment =
    MutableFragment(this, key)
