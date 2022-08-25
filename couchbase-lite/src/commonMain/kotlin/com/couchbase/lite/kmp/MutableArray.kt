@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

import kotlinx.datetime.Instant

/**
 * MutableArray provides access to array data.
 * This class and its constructor are referenced by name, from native code.
 */
public expect class MutableArray : Array {

    /**
     * Constructs a new empty Array object.
     */
    public constructor()

    /**
     * Creates a new MutableArray with content from the passed List.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.
     *
     * @param data the document content list
     */
    public constructor(data: List<Any?>)

    /**
     * Creates a new MutableArray with content from the passed JSON string.
     *
     * @param json the array content as a JSON string.
     */
    public constructor(json: String)

    /**
     * Populate an array with content from a Map.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.
     * Setting the array content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param data the array
     * @return The self object
     */
    public fun setData(data: List<Any?>): MutableArray

    /**
     * Populate an array with content from a JSON string.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.
     * Setting the array content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param json the dictionary object.
     * @return this Document instance
     */
    public fun setJSON(json: String): MutableArray

    /**
     * Set an object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the object
     * @return The self object
     */
    public fun setValue(index: Int, value: Any?): MutableArray

    /**
     * Sets an String object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the String object
     * @return The self object
     */
    public fun setString(index: Int, value: String?): MutableArray

    /**
     * Sets an NSNumber object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Number object
     * @return The self object
     */
    public fun setNumber(index: Int, value: Number?): MutableArray

    /**
     * Sets an integer value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the int value
     * @return The self object
     */
    public fun setInt(index: Int, value: Int): MutableArray

    /**
     * Sets an integer value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the long value
     * @return The self object
     */
    public fun setLong(index: Int, value: Long): MutableArray

    /**
     * Sets a float value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the float value
     * @return The self object
     */
    public fun setFloat(index: Int, value: Float): MutableArray

    /**
     * Sets a double value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the double value
     * @return The self object
     */
    public fun setDouble(index: Int, value: Double): MutableArray

    /**
     * Sets a boolean value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the boolean value
     * @return The self object
     */
    public fun setBoolean(index: Int, value: Boolean): MutableArray

    /**
     * Sets a Blob object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Blob object
     * @return The self object
     */
    public fun setBlob(index: Int, value: Blob?): MutableArray

    /**
     * Sets a Date object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Date object
     * @return The self object
     */
    public fun setDate(index: Int, value: Instant?): MutableArray

    /**
     * Sets a Array object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Array object
     * @return The self object
     */
    public fun setArray(index: Int, value: Array?): MutableArray

    /**
     * Sets a Dictionary object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Dictionary object
     * @return The self object
     */
    public fun setDictionary(index: Int, value: Dictionary?): MutableArray

    /**
     * Adds an object to the end of the array.
     *
     * @param value the object
     * @return The self object
     */
    public fun addValue(value: Any?): MutableArray

    /**
     * Adds a String object to the end of the array.
     *
     * @param value the String object
     * @return The self object
     */
    public fun addString(value: String?): MutableArray

    /**
     * Adds a Number object to the end of the array.
     *
     * @param value the Number object
     * @return The self object
     */
    public fun addNumber(value: Number?): MutableArray

    /**
     * Adds an integer value to the end of the array.
     *
     * @param value the int value
     * @return The self object
     */
    public fun addInt(value: Int): MutableArray

    /**
     * Adds a long value to the end of the array.
     *
     * @param value the long value
     * @return The self object
     */
    public fun addLong(value: Long): MutableArray

    /**
     * Adds a float value to the end of the array.
     *
     * @param value the float value
     * @return The self object
     */
    public fun addFloat(value: Float): MutableArray

    /**
     * Adds a double value to the end of the array.
     *
     * @param value the double value
     * @return The self object
     */
    public fun addDouble(value: Double): MutableArray

    /**
     * Adds a boolean value to the end of the array.
     *
     * @param value the boolean value
     * @return The self object
     */
    public fun addBoolean(value: Boolean): MutableArray

    /**
     * Adds a Blob object to the end of the array.
     *
     * @param value the Blob object
     * @return The self object
     */
    public fun addBlob(value: Blob?): MutableArray

    /**
     * Adds a Date object to the end of the array.
     *
     * @param value the Date object
     * @return The self object
     */
    public fun addDate(value: Instant?): MutableArray

    /**
     * Adds an Array object to the end of the array.
     *
     * @param value the Array object
     * @return The self object
     */
    public fun addArray(value: Array?): MutableArray

    /**
     * Adds a Dictionary object to the end of the array.
     *
     * @param value the Dictionary object
     * @return The self object
     */
    public fun addDictionary(value: Dictionary?): MutableArray

    /**
     * Inserts an object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the object
     * @return The self object
     */
    public fun insertValue(index: Int, value: Any?): MutableArray

    /**
     * Inserts a String object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the String object
     * @return The self object
     */
    public fun insertString(index: Int, value: String?): MutableArray

    /**
     * Inserts a Number object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Number object
     * @return The self object
     */
    public fun insertNumber(index: Int, value: Number?): MutableArray

    /**
     * Inserts an integer value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the int value
     * @return The self object
     */
    public fun insertInt(index: Int, value: Int): MutableArray

    /**
     * Inserts a long value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the long value
     * @return The self object
     */
    public fun insertLong(index: Int, value: Long): MutableArray

    /**
     * Inserts a float value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the float value
     * @return The self object
     */
    public fun insertFloat(index: Int, value: Float): MutableArray

    /**
     * Inserts a double value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the double value
     * @return The self object
     */
    public fun insertDouble(index: Int, value: Double): MutableArray

    /**
     * Inserts a boolean value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the boolean value
     * @return The self object
     */
    public fun insertBoolean(index: Int, value: Boolean): MutableArray

    /**
     * Inserts a Blob object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Blob object
     * @return The self object
     */
    public fun insertBlob(index: Int, value: Blob?): MutableArray

    /**
     * Inserts a Date object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Date object
     * @return The self object
     */
    public fun insertDate(index: Int, value: Instant?): MutableArray

    /**
     * Inserts an Array object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Array object
     * @return The self object
     */
    public fun insertArray(index: Int, value: Array?): MutableArray

    /**
     * Inserts a Dictionary object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Dictionary object
     * @return The self object
     */
    public fun insertDictionary(index: Int, value: Dictionary?): MutableArray

    /**
     * Removes the object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return The self object
     */
    public fun remove(index: Int): MutableArray

    /**
     * Gets a Array at the given index. Return null if the value is not an array.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Array object.
     */
    override fun getArray(index: Int): MutableArray?

    /**
     * Gets a Dictionary at the given index. Return null if the value is not an dictionary.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Dictionary object.
     */
    override fun getDictionary(index: Int): MutableDictionary?
}
