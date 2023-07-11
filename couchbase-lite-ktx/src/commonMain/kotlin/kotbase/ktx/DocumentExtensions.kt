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
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/main/java/com/molo17/couchbase/lite/DocumentExtensions.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Resolve explicitApiWarning() requirements
 * - Implement MutableDictionaryInterface explicitly (not part of KMP API)
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

public class DocumentBuilder internal constructor(
    private val document: MutableDocument = MutableDocument()
) {

    internal fun build() = document

    /**
     * Determines the key-to-value relation between the receiver string and the provided [value].
     */
    public infix fun <T> String.to(value: T) {
        setValue(this, value)
    }

    /**
     * Get a property's value as a Array, which is a mapping object of an array value.
     * Returns null if the property doesn't exists, or its value is not an array.
     *
     * @param key the key.
     * @return the Array object.
     */
    public fun getArray(key: String): MutableArray? =
        document.getArray(key)

    /**
     * Get a property's value as a Dictionary, which is a mapping object of an dictionary value.
     * Returns null if the property doesn't exists, or its value is not an dictionary.
     *
     * @param key the key.
     * @return the Dictionary object or null if the key doesn't exist.
     */
    public fun getDictionary(key: String): MutableDictionary? =
        document.getDictionary(key)

    /**
     * Removes the mapping for a key from this Dictionary
     *
     * @param key the key.
     * @return this MutableDocument instance
     */
    public fun remove(key: String): MutableDocument =
        document.remove(key)

    /**
     * Set a integer value for the given key
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
     * Set a Number value for the given key
     *
     * @param key   the key.
     * @param value the Number value.
     * @return this MutableDocument instance
     */
    public fun setNumber(key: String, value: Number): MutableDocument =
        document.setNumber(key, value)

    /**
     * Set a String value for the given key
     *
     * @param key   the key.
     * @param value the String value.
     * @return this MutableDocument instance
     */
    public fun setString(key: String, value: String): MutableDocument =
        document.setString(key, value)

    /**
     * Set a Date value for the given key
     *
     * @param key   the key.
     * @param value the Date value.
     * @return this MutableDocument instance
     */
    public fun setDate(key: String, value: Instant?): MutableDocument =
        document.setDate(key, value)

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
     * Set an object value by key. Allowed value types are List, Date, Map, Number, null, String,
     * Array, Blob, and Dictionary. If present, Lists, Maps and Dictionaries may contain only
     * the above types. A Date object will be converted to an ISO-8601 format string.
     *
     * @param key   the key.
     * @param value the Object value.
     * @return this Document instance
     */
    public fun setValue(key: String, value: Any?): MutableDocument =
        document.setValue(key, value)

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
     * Populate a document with content from a Map.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.  Setting the
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
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.  Setting the
     * document content will replace the current data including the existing Array and Dictionary
     * objects.
     *
     * @param json the dictionary object.
     * @return this Document instance
     */
    public fun setJSON(json: String): MutableDocument =
        document.setJSON(json)
}
