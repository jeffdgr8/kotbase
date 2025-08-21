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
 * Fragment provides readonly access to data value. Fragment also provides subscript access
 * by either key or index to the nested values which are wrapped by Fragment objects.
 */
public open class Fragment
internal constructor(
    protected val parent: Any?,
    protected val key: String?,
    protected val index: Int?
) {

    internal constructor(parent: DictionaryInterface, key: String) :
            this(parent, key, null)

    internal constructor(parent: ArrayInterface, index: Int) :
            this(parent, null, index)

    internal constructor() : this(null, null, null)

    protected open val dictParent: DictionaryInterface
        get() = parent as DictionaryInterface

    protected open val arrayParent: ArrayInterface
        get() = parent as ArrayInterface

    /**
     * Gets the fragment value. The value types are Blob, Array, Dictionary, Number,
     * or String based on the underlying data type; or null if the value is null.
     */
    public open val value: Any?
        get() = when {
            key != null -> dictParent.getValue(key)
            index != null -> arrayParent.getValue(index)
            else -> null
        }

    /**
     * Gets the value as a string. Returns null if the value is null, or the value is not a string.
     */
    public open val string: String?
        get() = when {
            key != null -> dictParent.getString(key)
            index != null -> arrayParent.getString(index)
            else -> null
        }

    /**
     * Gets the value as a Number. Returns null if the value
     * doesn't exist, or its value is not a Number.
     */
    public open val number: Number?
        get() = when {
            key != null -> dictParent.getNumber(key)
            index != null -> arrayParent.getNumber(index)
            else -> null
        }

    /**
     * Gets the value as an int. Floating point values will be rounded. The value true is
     * returned as 1, false as 0. Returns 0 if the value is null or is not a numeric value.
     */
    public open val int: Int
        get() = when {
            key != null -> dictParent.getInt(key)
            index != null -> arrayParent.getInt(index)
            else -> 0
        }

    /**
     * Gets the value as a long. Floating point values will be rounded. The value true is
     * returned as 1, false as 0. Returns 0 if the value is null or is not a numeric value.
     */
    public open val long: Long
        get() = when {
            key != null -> dictParent.getLong(key)
            index != null -> arrayParent.getLong(index)
            else -> 0L
        }

    /**
     * Gets the value as a float. Integers will be converted to float. The value true is
     * returned as 1.0, false as 0.0. Returns 0.0 if the value is null or is not a numeric value.
     */
    public open val float: Float
        get() = when {
            key != null -> dictParent.getFloat(key)
            index != null -> arrayParent.getFloat(index)
            else -> 0F
        }

    /**
     * Gets the value as a double. Integers will be converted to double. The value true is
     * returned as 1.0, false as 0.0. Returns 0.0 if the value is null or is not a numeric value.
     */
    public open val double: Double
        get() = when {
            key != null -> dictParent.getDouble(key)
            index != null -> arrayParent.getDouble(index)
            else -> 0.0
        }

    /**
     * Gets the value as a boolean. Returns true if the value
     * is not null, and is either true or a nonzero number.
     */
    public open val boolean: Boolean
        get() = when {
            key != null -> dictParent.getBoolean(key)
            index != null -> arrayParent.getBoolean(index)
            else -> false
        }

    /**
     * Get the value as a Blob. Returns null if the value is null, or the value is not a Blob.
     */
    public open val blob: Blob?
        get() = when {
            key != null -> dictParent.getBlob(key)
            index != null -> arrayParent.getBlob(index)
            else -> null
        }

    /**
     * Gets the value as a date. JSON does not directly support dates, so the actual property value
     * must be a string, which is then parsed according to the ISO-8601 date format (the default
     * used in JSON.) Returns null if the value is null, is not a string, or is not parseable as a
     * date. NOTE: This is not a generic date parser! It only recognizes the ISO-8601 format, with
     * or without milliseconds.
     */
    public open val date: Instant?
        get() = when {
            key != null -> dictParent.getDate(key)
            index != null -> arrayParent.getDate(index)
            else -> null
        }

    /**
     * Get the value as an Array, a mapping object of an array value.
     * Returns null if the value is null, or the value is not an array.
     */
    public open val array: Array?
        get() = when {
            key != null -> dictParent.getArray(key)
            index != null -> arrayParent.getArray(index)
            else -> null
        }

    /**
     * Get a property’s value as a Dictionary, a mapping object of a dictionary
     * value. Returns null if the value is null, or the value is not a dictionary.
     */
    public open val dictionary: Dictionary?
        get() = when {
            key != null -> dictParent.getDictionary(key)
            index != null -> arrayParent.getDictionary(index)
            else -> null
        }

    /**
     * Checks whether the value held by the fragment object exists or is null value or not.
     */
    public val exists: Boolean
        get() = value != null

    /**
     * Subscript access to a Fragment object by index.
     *
     * @param index The index. If the index value exceeds the bounds of the array,
     * the MutableFragment object will represent a null value.
     */
    public open operator fun get(index: Int): Fragment {
        val parent = when {
            key != null -> dictParent.getValue(key)
            this.index != null -> arrayParent.getValue(this.index)
            else -> null
        }
        return if (parent is ArrayInterface && index in 0..<parent.count) {
            Fragment(parent, null, index)
        } else {
            Fragment()
        }
    }

    /**
     * Subscript access to a Fragment object by key.
     *
     * @param key The key.
     */
    public open operator fun get(key: String): Fragment {
        val parent = when {
            this.key != null -> dictParent.getValue(this.key)
            index != null -> arrayParent.getValue(index)
            else -> null
        }
        return if (parent is DictionaryInterface) {
            Fragment(parent, key, null)
        } else {
            Fragment()
        }
    }
}
