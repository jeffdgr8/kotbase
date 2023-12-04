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
 * Mutable access to array data.
 */
public expect class MutableArray : Array {

    /**
     * Construct a new empty Array object.
     */
    public constructor()

    /**
     * Creates a new MutableArray with content from the passed List.
     * Allowed value types are List, Date, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Maps and Dictionaries may contain only the above types.
     *
     * @param data the array content list
     */
    public constructor(data: List<Any?>)

    /**
     * Creates a new MutableArray with content from the passed JSON string.
     *
     * @param json the array content as a JSON string.
     */
    public constructor(json: String)

    /**
     * Populate an array with content from a List.
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
     * Sets a String object at the given index.
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
     * Sets an Array object at the given index.
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
     * Gets an Array at the given index. Return null if the value is not an array.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Array object.
     */
    override fun getArray(index: Int): MutableArray?

    /**
     * Gets a Dictionary at the given index. Return null if the value is not a dictionary.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return the Dictionary object.
     */
    override fun getDictionary(index: Int): MutableDictionary?
}

/**
 * Subscripting access to a MutableFragment object that represents the value at the given index.
 *
 * @param index The index. If the index value exceeds the bounds of the array,
 * the MutableFragment object will represent a nil value.
 */
public operator fun MutableArray.get(index: Int): MutableFragment {
    return if (index in 0..<count) {
        MutableFragment(this, index)
    } else {
        MutableFragment()
    }
}
