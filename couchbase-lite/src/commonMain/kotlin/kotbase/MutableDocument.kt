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

/**
 * A Couchbase Lite Document. A document has key/value properties like a Map.
 */
public expect class MutableDocument : Document, MutableDictionaryInterface {

    /**
     * Creates a new Document object with a new random UUID. The created document will be
     * saved into a database when you call the Database's save(Document) method with the document
     * object given.
     */
    public constructor()

    /**
     * Creates a new Document with the given ID. If the id is null, the document
     * will be created with a new random UUID. The created document will be
     * saved into a database when you call the Database's save(Document) method with the document
     * object given.
     *
     * @param id the document ID or null.
     */
    public constructor(id: String?)

    /**
     * Creates a new Document with a new random UUID and the map as the content.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
     * The created document will be saved into a database when you call Database.save(Document)
     * with this document object.
     *
     * @param data the Map object
     */
    public constructor(data: Map<String, Any?>)

    /**
     * Creates a new Document with a given ID and content from the passed Map.
     * If the id is null, the document will be created with a new random UUID.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * The List and Map must contain only the above types.
     * The created document will be saved into a database when you call
     * the Database's save(Document) method with the document object given.
     *
     * @param id   the document ID.
     * @param data the Map object
     */
    public constructor(id: String?, data: Map<String, Any?>)

    /**
     * Creates a new Document with the given ID and content from the passed JSON string.
     * If the id is null, the document will be created with a new random UUID.
     * The created document will be saved into a database when you call the Database's
     * save(Document) method with the document object given.
     *
     * @param id   the document ID or null.
     * @param json the document content as a JSON string.
     */
    public constructor(id: String?, json: String)

    /**
     * Returns the copy of this MutableDocument object.
     *
     * @return The MutableDocument object
     */
    override fun toMutable(): MutableDocument

    /**
     * Populate a document with content from a Map.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types. Setting the
     * document content will replace the current data, including the existing Array and Dictionary
     * objects.
     *
     * @param data the dictionary object.
     * @return this Document instance
     */
    override fun setData(data: Map<String, Any?>): MutableDocument

    /**
     * Populate a document with content from a JSON string.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types. Setting the
     * document content will replace the current data, including the existing Array and Dictionary
     * objects.
     *
     * @param json the dictionary object.
     * @return this Document instance
     */
    override fun setJSON(json: String): MutableDocument

    /**
     * Set an object value by key. Allowed value types are List, Instant, Map, Number, null, String,
     * Array, Blob, and Dictionary. If present, Lists, Arrays, Maps and Dictionaries may contain only
     * the above types. An Instant date object will be converted to an ISO-8601 format string.
     *
     * @param key   the key.
     * @param value the Object value.
     * @return this Document instance
     */
    override fun setValue(key: String, value: Any?): MutableDocument

    /**
     * Set a String value for the given key
     *
     * @param key   the key.
     * @param value the String value.
     * @return this MutableDocument instance
     */
    override fun setString(key: String, value: String?): MutableDocument

    /**
     * Set a Number value for the given key
     *
     * @param key   the key.
     * @param value the Number value.
     * @return this MutableDocument instance
     */
    override fun setNumber(key: String, value: Number?): MutableDocument

    /**
     * Set an integer value for the given key
     *
     * @param key   the key.
     * @param value the integer value.
     * @return this MutableDocument instance
     */
    override fun setInt(key: String, value: Int): MutableDocument

    /**
     * Set a long value for the given key
     *
     * @param key   the key.
     * @param value the long value.
     * @return this MutableDocument instance
     */
    override fun setLong(key: String, value: Long): MutableDocument

    /**
     * Set a float value for the given key
     *
     * @param key   the key.
     * @param value the float value.
     * @return this MutableDocument instance
     */
    override fun setFloat(key: String, value: Float): MutableDocument

    /**
     * Set a double value for the given key
     *
     * @param key   the key.
     * @param value the double value.
     * @return this MutableDocument instance
     */
    override fun setDouble(key: String, value: Double): MutableDocument

    /**
     * Set a boolean value for the given key
     *
     * @param key   the key.
     * @param value the boolean value.
     * @return this MutableDocument instance
     */
    override fun setBoolean(key: String, value: Boolean): MutableDocument

    /**
     * Set a Blob value for the given key
     *
     * @param key   the key.
     * @param value the Blob value.
     * @return this MutableDocument instance
     */
    override fun setBlob(key: String, value: Blob?): MutableDocument

    /**
     * Set an Instant date value for the given key
     *
     * @param key   the key.
     * @param value the Date value.
     * @return this MutableDocument instance
     */
    override fun setDate(key: String, value: Instant?): MutableDocument

    /**
     * Set an Array value for the given key
     *
     * @param key   the key.
     * @param value the Array value.
     * @return this MutableDocument instance
     */
    override fun setArray(key: String, value: Array?): MutableDocument

    /**
     * Set a Dictionary value for the given key
     *
     * @param key   the key.
     * @param value the Dictionary value.
     * @return this MutableDocument instance
     */
    override fun setDictionary(key: String, value: Dictionary?): MutableDocument

    /**
     * Removes the mapping for a key from this Document
     *
     * @param key the key.
     * @return this MutableDocument instance
     */
    override fun remove(key: String): MutableDocument

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
}
