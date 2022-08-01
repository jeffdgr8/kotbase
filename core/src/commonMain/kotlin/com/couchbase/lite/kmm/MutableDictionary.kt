package com.couchbase.lite.kmm

import kotlinx.datetime.Instant

/**
 * Dictionary provides access to dictionary data.
 */
public expect class MutableDictionary : Dictionary {

    /**
     * Initialize a new empty Dictionary object.
     */
    public constructor()

    /**
     * Creates a new MutableDictionary with content from the passed Map.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.
     *
     * @param data the dictionary content map.
     */
    public constructor(data: Map<String, Any?>)

    /**
     * Creates a new MutableDictionary with content from the passed JSON string.
     *
     * @param json the dictionary content as a JSON string.
     */
    public constructor(json: String)

    /**
     * Populate a dictionary with content from a Map.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.
     * Setting the dictionary content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param data the dictionary object.
     * @return The self object.
     */
    public fun setData(data: Map<String, Any?>): MutableDictionary

    /**
     * Populate a dictionary with content from a JSON string.
     * Setting the dictionary content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param json the dictionary object.
     * @return this Document instance
     */
    public fun setJSON(json: String): MutableDictionary

    /**
     * Set an object value by key.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.
     *
     * @param key   the key.
     * @param value the object value.
     * @return The self object.
     */
    public fun setValue(key: String, value: Any?): MutableDictionary

    /**
     * Set a String value for the given key.
     *
     * @param key   The key
     * @param value The String value.
     * @return The self object.
     */
    public fun setString(key: String, value: String?): MutableDictionary

    /**
     * Set a Number value for the given key.
     *
     * @param key   The key
     * @param value The number value.
     * @return The self object.
     */
    public fun setNumber(key: String, value: Number?): MutableDictionary

    /**
     * Set an int value for the given key.
     *
     * @param key   The key
     * @param value The int value.
     * @return The self object.
     */
    public fun setInt(key: String, value: Int): MutableDictionary

    /**
     * Set a long value for the given key.
     *
     * @param key   The key
     * @param value The long value.
     * @return The self object.
     */
    public fun setLong(key: String, value: Long): MutableDictionary

    /**
     * Set a float value for the given key.
     *
     * @param key   The key
     * @param value The float value.
     * @return The self object.
     */
    public fun setFloat(key: String, value: Float): MutableDictionary

    /**
     * Set a double value for the given key.
     *
     * @param key   The key
     * @param value The double value.
     * @return The self object.
     */
    public fun setDouble(key: String, value: Double): MutableDictionary

    /**
     * Set a boolean value for the given key.
     *
     * @param key   The key
     * @param value The boolean value.
     * @return The self object.
     */
    public fun setBoolean(key: String, value: Boolean): MutableDictionary

    /**
     * Set a Blob object for the given key.
     *
     * @param key   The key
     * @param value The Blob object.
     * @return The self object.
     */
    public fun setBlob(key: String, value: Blob?): MutableDictionary

    /**
     * Set a Date object for the given key.
     *
     * @param key   The key
     * @param value The Date object.
     * @return The self object.
     */
    public fun setDate(key: String, value: Instant?): MutableDictionary

    /**
     * Set an Array object for the given key.
     *
     * @param key   The key
     * @param value The Array object.
     * @return The self object.
     */
    public fun setArray(key: String, value: Array?): MutableDictionary

    /**
     * Set a Dictionary object for the given key.
     *
     * @param key   The key
     * @param value The Dictionary object.
     * @return The self object.
     */
    public fun setDictionary(key: String, value: Dictionary?): MutableDictionary

    /**
     * Removes the mapping for a key from this Dictionary
     *
     * @param key the key.
     * @return The self object.
     */
    public fun remove(key: String): MutableDictionary

    /**
     * Get a property's value as a Array, which is a mapping object of an array value.
     * Returns null if the property doesn't exists, or its value is not an array.
     *
     * @param key the key.
     * @return the Array object.
     */
    override fun getArray(key: String): MutableArray?

    /**
     * Get a property's value as a Dictionary, which is a mapping object of an dictionary value.
     * Returns null if the property doesn't exists, or its value is not an dictionary.
     *
     * @param key the key.
     * @return the Dictionary object or null if the key doesn't exist.
     */
    override fun getDictionary(key: String): MutableDictionary?
}
