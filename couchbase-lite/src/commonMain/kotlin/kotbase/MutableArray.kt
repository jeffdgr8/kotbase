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
 * Mutable access to array data.
 */
public expect class MutableArray : Array, MutableArrayInterface {

    /**
     * Construct a new empty MutableArray.
     */
    public constructor()

    /**
     * Creates a new MutableArray with content from the passed List.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
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

    override fun setData(data: List<Any?>): MutableArray

    override fun setJSON(json: String): MutableArray

    override fun setValue(index: Int, value: Any?): MutableArray

    override fun setString(index: Int, value: String?): MutableArray

    override fun setNumber(index: Int, value: Number?): MutableArray

    override fun setInt(index: Int, value: Int): MutableArray

    override fun setLong(index: Int, value: Long): MutableArray

    override fun setFloat(index: Int, value: Float): MutableArray

    override fun setDouble(index: Int, value: Double): MutableArray

    override fun setBoolean(index: Int, value: Boolean): MutableArray

    override fun setBlob(index: Int, value: Blob?): MutableArray

    override fun setDate(index: Int, value: Instant?): MutableArray

    override fun setArray(index: Int, value: Array?): MutableArray

    override fun setDictionary(index: Int, value: Dictionary?): MutableArray

    override fun addValue(value: Any?): MutableArray

    override fun addString(value: String?): MutableArray

    override fun addNumber(value: Number?): MutableArray

    override fun addInt(value: Int): MutableArray

    override fun addLong(value: Long): MutableArray

    override fun addFloat(value: Float): MutableArray

    override fun addDouble(value: Double): MutableArray

    override fun addBoolean(value: Boolean): MutableArray

    override fun addBlob(value: Blob?): MutableArray

    override fun addDate(value: Instant?): MutableArray

    override fun addArray(value: Array?): MutableArray

    override fun addDictionary(value: Dictionary?): MutableArray

    override fun insertValue(index: Int, value: Any?): MutableArray

    override fun insertString(index: Int, value: String?): MutableArray

    override fun insertNumber(index: Int, value: Number?): MutableArray

    override fun insertInt(index: Int, value: Int): MutableArray

    override fun insertLong(index: Int, value: Long): MutableArray

    override fun insertFloat(index: Int, value: Float): MutableArray

    override fun insertDouble(index: Int, value: Double): MutableArray

    override fun insertBoolean(index: Int, value: Boolean): MutableArray

    override fun insertBlob(index: Int, value: Blob?): MutableArray

    override fun insertDate(index: Int, value: Instant?): MutableArray

    override fun insertArray(index: Int, value: Array?): MutableArray

    override fun insertDictionary(index: Int, value: Dictionary?): MutableArray

    override fun remove(index: Int): MutableArray

    override fun getArray(index: Int): MutableArray?

    override fun getDictionary(index: Int): MutableDictionary?
}
