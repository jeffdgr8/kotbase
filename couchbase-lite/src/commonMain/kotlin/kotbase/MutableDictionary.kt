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
 * Mutable access to dictionary data.
 */
public expect class MutableDictionary : Dictionary, MutableDictionaryInterface {

    /**
     * Construct a new empty MutableDictionary.
     */
    public constructor()

    /**
     * Creates a new MutableDictionary with content from the passed Map.
     * Allowed value types are List, Instant, Map, Number, null, String, Array, Blob, and Dictionary.
     * If present, Lists, Arrays, Maps and Dictionaries may contain only the above types.
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

    override fun setData(data: Map<String, Any?>): MutableDictionary

    override fun setJSON(json: String): MutableDictionary

    override fun setValue(key: String, value: Any?): MutableDictionary

    override fun setString(key: String, value: String?): MutableDictionary

    override fun setNumber(key: String, value: Number?): MutableDictionary

    override fun setInt(key: String, value: Int): MutableDictionary

    override fun setLong(key: String, value: Long): MutableDictionary

    override fun setFloat(key: String, value: Float): MutableDictionary

    override fun setDouble(key: String, value: Double): MutableDictionary

    override fun setBoolean(key: String, value: Boolean): MutableDictionary

    override fun setBlob(key: String, value: Blob?): MutableDictionary

    override fun setDate(key: String, value: Instant?): MutableDictionary

    override fun setArray(key: String, value: Array?): MutableDictionary

    override fun setDictionary(key: String, value: Dictionary?): MutableDictionary

    override fun remove(key: String): MutableDictionary

    override fun getArray(key: String): MutableArray?

    override fun getDictionary(key: String): MutableDictionary?
}

/**
 * Subscripting access to a MutableFragment object that represents the value of the dictionary by key.
 *
 * @param key The key.
 */
public operator fun MutableDictionary.get(key: String): MutableFragment =
    MutableFragment(this, key)
