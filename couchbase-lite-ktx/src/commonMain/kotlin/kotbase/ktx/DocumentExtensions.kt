/*
 * Copyright (c) 2020 MOLO17
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/*
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/main/java/com/molo17/couchbase/lite/DocumentExtensions.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Resolve explicitApiWarning() requirements
 * - Implement DictionaryInterface and MutableDictionaryInterface explicitly (not part of KMP API)
 */

package kotbase.ktx

import kotbase.*
import kotbase.Array
import kotlinx.datetime.Instant

/**
 * Creates a new [MutableDocument] with the key-value entries specified by the
 * given [block] function.
 *
 * Example of usage:
 *
 * ```
 * val document = MutableDocument {
 *   "name" to "John"
 *   "surname" to "Doe"
 *   "type" to "user"
 * }
 * ```
 *
 * @return a [MutableDocument] instance
 */
public fun MutableDocument(block: DocumentBuilder.() -> Unit): MutableDocument =
    DocumentBuilder().apply(block).build()

public fun MutableDocument(id: String?, block: DocumentBuilder.() -> Unit): MutableDocument =
    DocumentBuilder(id).apply(block).build()

public class DocumentBuilder internal constructor(id: String? = null) {

    private val document: MutableDocument = MutableDocument(id)

    internal fun build() = document

    /**
     * Determines the key-to-value relation between the receiver string and the provided [value].
     */
    public infix fun String.to(value: Any?) {
        setValue(this, value)
    }

    /**
     * The number of the entries in the document.
     */
    public val count: Int =
        document.count

    /**
     * A List containing all keys, or an empty List if the document has no properties.
     */
    public val keys: List<String> =
        document.keys

    /**
     * Gets a property's value as an object. The object types are Blob, Array,
     * Dictionary, Number, or String based on the underlying data type; or nil if the
     * property value is null or the property doesn't exist.
     *
     * @param key the key.
     * @return the object value or null.
     */
    public fun getValue(key: String): Any? =
        document.getValue(key)

    /**
     * Gets a property's value as a String.
     * Returns null if the value doesn't exist, or its value is not a String.
     *
     * @param key the key
     * @return the String or null.
     */
    public fun getString(key: String): String? =
        document.getString(key)

    /**
     * Gets a property's value as a Number.
     * Returns null if the value doesn't exist, or its value is not a Number.
     *
     * @param key the key
     * @return the Number or nil.
     */
    public fun getNumber(key: String): Number? =
        document.getNumber(key)

    /**
     * Gets a property's value as an int.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the int value.
     */
    public fun getInt(key: String): Int =
        document.getInt(key)

    /**
     * Gets a property's value as a long.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the long value.
     */
    public fun getLong(key: String): Long =
        document.getLong(key)

    /**
     * Gets a property's value as a float.
     * Integers will be converted to float. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the float value.
     */
    public fun getFloat(key: String): Float =
        document.getFloat(key)

    /**
     * Gets a property's value as a double.
     * Integers will be converted to double. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the property doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the double value.
     */
    public fun getDouble(key: String): Double =
        document.getDouble(key)

    /**
     * Gets a property's value as a boolean. Returns true if the value exists, and is either `true`
     * or a nonzero number.
     *
     * @param key the key
     * @return the boolean value.
     */
    public fun getBoolean(key: String): Boolean =
        document.getBoolean(key)

    /**
     * Gets a property's value as a Blob.
     * Returns null if the value doesn't exist, or its value is not a Blob.
     *
     * @param key the key
     * @return the Blob value or null.
     */
    public fun getBlob(key: String): Blob? =
        document.getBlob(key)

    /**
     * Gets a property's value as an Instant date.
     * JSON does not directly support dates, so the actual property value must be a string, which is
     * then parsed according to the ISO-8601 date format (the default used in JSON.)
     * Returns null if the value doesn't exist, is not a string, or is not parsable as a date.
     * NOTE: This is not a generic date parser! It only recognizes the ISO-8601 format, with or
     * without milliseconds.
     *
     * @param key the key
     * @return the Instant date value or null.
     */
    public fun getDate(key: String): Instant? =
        document.getDate(key)

    /**
     * Gets content of the current object as a Map. The values contained in the returned
     * Map object are all JSON based values.
     *
     * @return the Map object representing the content of the current object in the JSON format.
     */
    public fun toMap(): Map<String, Any?> =
        document.toMap()

    /**
     * Tests whether a property exists or not.
     * This can be less expensive than getValue(String),
     * because it does not have to allocate an Object for the property value.
     *
     * @param key the key
     * @return the boolean value representing whether a property exists or not.
     */
    public operator fun contains(key: String): Boolean =
        document.contains(key)

    /**
     * Populate a document with content from a Map.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types. Setting the
     * document content will replace the current data including the existing Array and Dictionary
     * objects.
     *
     * @param data the dictionary object.
     * @return this Document instance
     */
    public fun setData(data: Map<String, Any?>): MutableDocument =
        document.setData(data)

    /**
     * Populate a document with content from a JSON string.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types. Setting the
     * document content will replace the current data including the existing Array and Dictionary
     * objects.
     *
     * @param json the dictionary object.
     * @return this Document instance
     */
    public fun setJSON(json: String): MutableDocument =
        document.setJSON(json)

    /**
     * Set an object value by key. Allowed value types are List, Instant, Map, Number, null, String,
     * Array, Blob, and Dictionary. If present, Lists, Arrays, Maps and Dictionaries may contain only
     * the above types. An Instant date object will be converted to an ISO-8601 format string.
     *
     * @param key   the key.
     * @param value the Object value.
     * @return this Document instance
     */
    public fun setValue(key: String, value: Any?): MutableDocument =
        document.setValue(key, value)

    /**
     * Set a String value for the given key
     *
     * @param key   the key.
     * @param value the String value.
     * @return this MutableDocument instance
     */
    public fun setString(key: String, value: String?): MutableDocument =
        document.setString(key, value)

    /**
     * Set a Number value for the given key
     *
     * @param key   the key.
     * @param value the Number value.
     * @return this MutableDocument instance
     */
    public fun setNumber(key: String, value: Number?): MutableDocument =
        document.setNumber(key, value)

    /**
     * Set an integer value for the given key
     *
     * @param key   the key.
     * @param value the integer value.
     * @return this MutableDocument instance
     */
    public fun setInt(key: String, value: Int): MutableDocument =
        document.setInt(key, value)

    /**
     * Set a long value for the given key
     *
     * @param key   the key.
     * @param value the long value.
     * @return this MutableDocument instance
     */
    public fun setLong(key: String, value: Long): MutableDocument =
        document.setLong(key, value)

    /**
     * Set a float value for the given key
     *
     * @param key   the key.
     * @param value the float value.
     * @return this MutableDocument instance
     */
    public fun setFloat(key: String, value: Float): MutableDocument =
        document.setFloat(key, value)

    /**
     * Set a double value for the given key
     *
     * @param key   the key.
     * @param value the double value.
     * @return this MutableDocument instance
     */
    public fun setDouble(key: String, value: Double): MutableDocument =
        document.setDouble(key, value)

    /**
     * Set a boolean value for the given key
     *
     * @param key   the key.
     * @param value the boolean value.
     * @return this MutableDocument instance
     */
    public fun setBoolean(key: String, value: Boolean): MutableDocument =
        document.setBoolean(key, value)

    /**
     * Set a Blob value for the given key
     *
     * @param key   the key.
     * @param value the Blob value.
     * @return this MutableDocument instance
     */
    public fun setBlob(key: String, value: Blob?): MutableDocument =
        document.setBlob(key, value)

    /**
     * Set an Instant date value for the given key
     *
     * @param key   the key.
     * @param value the Date value.
     * @return this MutableDocument instance
     */
    public fun setDate(key: String, value: Instant?): MutableDocument =
        document.setDate(key, value)

    /**
     * Set an Array value for the given key
     *
     * @param key   the key.
     * @param value the Array value.
     * @return this MutableDocument instance
     */
    public fun setArray(key: String, value: Array?): MutableDocument =
        document.setArray(key, value)

    /**
     * Set a Dictionary value for the given key
     *
     * @param key   the key.
     * @param value the Dictionary value.
     * @return this MutableDocument instance
     */
    public fun setDictionary(key: String, value: Dictionary?): MutableDocument =
        document.setDictionary(key, value)

    /**
     * Removes the mapping for a key from this Dictionary
     *
     * @param key the key.
     * @return this MutableDocument instance
     */
    public fun remove(key: String): MutableDocument =
        document.remove(key)

    /**
     * Get a property's value as an Array.
     * Returns null if the property doesn't exist, or its value is not an array.
     *
     * @param key the key.
     * @return the Array object.
     */
    public fun getArray(key: String): MutableArray? =
        document.getArray(key)

    /**
     * Get a property's value as a Dictionary.
     * Returns null if the property doesn't exist, or its value is not a dictionary.
     *
     * @param key the key.
     * @return the Dictionary object or null if the key doesn't exist.
     */
    public fun getDictionary(key: String): MutableDictionary? =
        document.getDictionary(key)
}
