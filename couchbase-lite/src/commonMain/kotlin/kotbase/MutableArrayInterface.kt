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
 * MutableArrayInterface defines a set of methods for getting and setting array data.
 */
public interface MutableArrayInterface : ArrayInterface {

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

    // remove

    /**
     * Removes the object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @return The self object
     */
    public fun remove(index: Int): MutableArrayInterface

    // set

    /**
     * Sets an integer value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the int value
     * @return The self object
     */
    public fun setInt(index: Int, value: Int): MutableArrayInterface

    /**
     * Sets an integer value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the long value
     * @return The self object
     */
    public fun setLong(index: Int, value: Long): MutableArrayInterface

    /**
     * Sets a float value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the float value
     * @return The self object
     */
    public fun setFloat(index: Int, value: Float): MutableArrayInterface

    /**
     * Sets a double value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the double value
     * @return The self object
     */
    public fun setDouble(index: Int, value: Double): MutableArrayInterface

    /**
     * Sets a boolean value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the boolean value
     * @return The self object
     */
    public fun setBoolean(index: Int, value: Boolean): MutableArrayInterface

    /**
     * Sets an Instant date object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Instant date object
     * @return The self object
     */
    public fun setDate(index: Int, value: Instant?): MutableArrayInterface

    /**
     * Sets a Blob object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Blob object
     * @return The self object
     */
    public fun setBlob(index: Int, value: Blob?): MutableArrayInterface

    /**
     * Sets an Array object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Array object
     * @return The self object
     */
    public fun setArray(index: Int, value: Array?): MutableArrayInterface

    /**
     * Sets a Dictionary object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Dictionary object
     * @return The self object
     */
    public fun setDictionary(index: Int, value: Dictionary?): MutableArrayInterface

    /**
     * Sets a String object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the String object
     * @return The self object
     */
    public fun setString(index: Int, value: String?): MutableArrayInterface

    /**
     * Sets an NSNumber object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Number object
     * @return The self object
     */
    public fun setNumber(index: Int, value: Number?): MutableArrayInterface

    /**
     * Set an object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the object
     * @return The self object
     */
    public fun setValue(index: Int, value: Any?): MutableArrayInterface

    /**
     * Populate an array with content from a List.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
     * Setting the array content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param data the array
     * @return The self object
     */
    public fun setData(data: List<Any?>): MutableArrayInterface

    /**
     * Populate an array with content from a JSON string.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
     * Setting the array content will replace the current data including
     * any existing Array and Dictionary objects.
     *
     * @param json the dictionary object.
     * @return this Document instance
     */
    public fun setJSON(json: String): MutableArrayInterface

    // add

    /**
     * Adds an integer value to the end of the array.
     *
     * @param value the int value
     * @return The self object
     */
    public fun addInt(value: Int): MutableArrayInterface

    /**
     * Adds a long value to the end of the array.
     *
     * @param value the long value
     * @return The self object
     */
    public fun addLong(value: Long): MutableArrayInterface

    /**
     * Adds a float value to the end of the array.
     *
     * @param value the float value
     * @return The self object
     */
    public fun addFloat(value: Float): MutableArrayInterface

    /**
     * Adds a double value to the end of the array.
     *
     * @param value the double value
     * @return The self object
     */
    public fun addDouble(value: Double): MutableArrayInterface

    /**
     * Adds a boolean value to the end of the array.
     *
     * @param value the boolean value
     * @return The self object
     */
    public fun addBoolean(value: Boolean): MutableArrayInterface

    /**
     * Adds a String object to the end of the array.
     *
     * @param value the String object
     * @return The self object
     */
    public fun addString(value: String?): MutableArrayInterface

    /**
     * Adds a Number object to the end of the array.
     *
     * @param value the Number object
     * @return The self object
     */
    public fun addNumber(value: Number?): MutableArrayInterface

    /**
     * Adds an Instant date object to the end of the array.
     *
     * @param value the Instant date object
     * @return The self object
     */
    public fun addDate(value: Instant?): MutableArrayInterface

    /**
     * Adds a Blob object to the end of the array.
     *
     * @param value the Blob object
     * @return The self object
     */
    public fun addBlob(value: Blob?): MutableArrayInterface

    /**
     * Adds an Array object to the end of the array.
     *
     * @param value the Array object
     * @return The self object
     */
    public fun addArray(value: Array?): MutableArrayInterface

    /**
     * Adds a Dictionary object to the end of the array.
     *
     * @param value the Dictionary object
     * @return The self object
     */
    public fun addDictionary(value: Dictionary?): MutableArrayInterface

    /**
     * Adds an object to the end of the array.
     *
     * @param value the object
     * @return The self object
     */
    public fun addValue(value: Any?): MutableArrayInterface

    // insert

    /**
     * Inserts an integer value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the int value
     * @return The self object
     */
    public fun insertInt(index: Int, value: Int): MutableArrayInterface

    /**
     * Inserts a long value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the long value
     * @return The self object
     */
    public fun insertLong(index: Int, value: Long): MutableArrayInterface

    /**
     * Inserts a float value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the float value
     * @return The self object
     */
    public fun insertFloat(index: Int, value: Float): MutableArrayInterface

    /**
     * Inserts a double value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the double value
     * @return The self object
     */
    public fun insertDouble(index: Int, value: Double): MutableArrayInterface

    /**
     * Inserts a boolean value at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the boolean value
     * @return The self object
     */
    public fun insertBoolean(index: Int, value: Boolean): MutableArrayInterface

    /**
     * Inserts an Instant date object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Instant date object
     * @return The self object
     */
    public fun insertDate(index: Int, value: Instant?): MutableArrayInterface

    /**
     * Inserts a Blob object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Blob object
     * @return The self object
     */
    public fun insertBlob(index: Int, value: Blob?): MutableArrayInterface

    /**
     * Inserts a Number object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Number object
     * @return The self object
     */
    public fun insertNumber(index: Int, value: Number?): MutableArrayInterface

    /**
     * Inserts a String object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the String object
     * @return The self object
     */
    public fun insertString(index: Int, value: String?): MutableArrayInterface

    /**
     * Inserts an Array object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Array object
     * @return The self object
     */
    public fun insertArray(index: Int, value: Array?): MutableArrayInterface

    /**
     * Inserts a Dictionary object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the Dictionary object
     * @return The self object
     */
    public fun insertDictionary(index: Int, value: Dictionary?): MutableArrayInterface

    /**
     * Inserts an object at the given index.
     *
     * @param index the index. This value must not exceed the bounds of the array.
     * @param value the object
     * @return The self object
     */
    public fun insertValue(index: Int, value: Any?): MutableArrayInterface
}

/**
 * Subscript access to a MutableFragment object of the projecting result
 * value at the given index.
 *
 * @param index The select result index. If the index value exceeds the bounds
 * of the array, the MutableFragment object will represent a null value.
 */
public operator fun MutableArray.get(index: Int): MutableFragment {
    return if (index in 0..<count) {
        MutableFragment(this, index)
    } else {
        MutableFragment()
    }
}
