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
 * MutableFragment provides read and write access to data value.
 * MutableFragment also provides subscript access by either key or index
 * to the nested values which are wrapped by MutableFragment objects.
 */
public class MutableFragment
internal constructor(
    parent: Any?,
    key: String?,
    index: Int?
) : Fragment(parent, key, index) {

    internal constructor(parent: MutableDictionaryInterface, key: String) :
            this(parent, key, null)

    internal constructor(parent: MutableArrayInterface, index: Int) :
            this(parent, null, index)

    internal constructor() : this(null, null, null)

    override val dictParent: MutableDictionaryInterface
        get() = parent as MutableDictionaryInterface

    override val arrayParent: MutableArrayInterface
        get() = parent as MutableArrayInterface

    /**
     * Gets the value from or sets the value to the fragment object.
     */
    override var value: Any?
        get() = super.value
        set(value) {
            when {
                key != null -> dictParent.setValue(key, value)
                index != null -> arrayParent.setValue(index, value)
            }
        }

    /**
     * Gets the value as string or sets the string value to the fragment object.
     */
    override var string: String?
        get() = super.string
        set(value) {
            when {
                key != null -> dictParent.setString(key, value)
                index != null -> arrayParent.setString(index, value)
            }
        }

    /**
     * Gets the value as a number or sets the number value to the fragment object.
     */
    override var number: Number?
        get() = super.number
        set(value) {
            when {
                key != null -> dictParent.setNumber(key, value)
                index != null -> arrayParent.setNumber(index, value)
            }
        }

    /**
     * Gets the value as integer or sets the integer value to the fragment object.
     */
    override var int: Int
        get() = super.int
        set(value) {
            when {
                key != null -> dictParent.setInt(key, value)
                index != null -> arrayParent.setInt(index, value)
            }
        }

    /**
     * Gets the value as long or sets the long value to the fragment object.
     */
    override var long: Long
        get() = super.long
        set(value) {
            when {
                key != null -> dictParent.setLong(key, value)
                index != null -> arrayParent.setLong(index, value)
            }
        }

    /**
     * Gets the value as float or sets the float value to the fragment object.
     */
    override var float: Float
        get() = super.float
        set(value) {
            when {
                key != null -> dictParent.setFloat(key, value)
                index != null -> arrayParent.setFloat(index, value)
            }
        }

    /**
     * Gets the value as double or sets the double value to the fragment object.
     */
    override var double: Double
        get() = super.double
        set(value) {
            when {
                key != null -> dictParent.setDouble(key, value)
                index != null -> arrayParent.setDouble(index, value)
            }
        }

    /**
     * Gets the value as boolean or sets the boolean value to the fragment object.
     */
    override var boolean: Boolean
        get() = super.boolean
        set(value) {
            when {
                key != null -> dictParent.setBoolean(key, value)
                index != null -> arrayParent.setBoolean(index, value)
            }
        }

    /**
     * Gets the value as blob or sets the blob value to the fragment object.
     */
    override var blob: Blob?
        get() = super.blob
        set(value) {
            when {
                key != null -> dictParent.setBlob(key, value)
                index != null -> arrayParent.setBlob(index, value)
            }
        }

    /**
     * Gets the value as date or sets the date value to the fragment object.
     */
    override var date: Instant?
        get() = super.date
        set(value) {
            when {
                key != null -> dictParent.setDate(key, value)
                index != null -> arrayParent.setDate(index, value)
            }
        }

    /**
     * Get the value as a MutableArray object, a mapping object of an array
     * value. Returns null if the value is null, or the value is not an array.
     */
    override var array: MutableArray?
        get() = when {
            key != null -> dictParent.getArray(key)
            index != null -> arrayParent.getArray(index)
            else -> null
        }
        set(value) {
            when {
                key != null -> dictParent.setArray(key, value)
                index != null -> arrayParent.setArray(index, value)
            }
        }

    /**
     * Get the value as a MutableDictionary object, a mapping object of a dictionary
     * value. Returns null if the value is null, or the value is not a dictionary.
     */
    override var dictionary: MutableDictionary?
        get() = when {
            key != null -> dictParent.getDictionary(key)
            index != null -> arrayParent.getDictionary(index)
            else -> null
        }
        set(value) {
            when {
                key != null -> dictParent.setDictionary(key, value)
                index != null -> arrayParent.setDictionary(index, value)
            }
        }

    /**
     * Subscript access to a Fragment object by index.
     *
     * @param index The index. If the index value exceeds the bounds of the array,
     * the MutableFragment object will represent a null value.
     */
    override fun get(index: Int): MutableFragment {
        val parent = when {
            key != null -> dictParent.getValue(key)
            this.index != null -> arrayParent.getValue(this.index)
            else -> null
        }
        return if (parent is MutableArrayInterface && index in 0..<parent.count) {
            MutableFragment(parent, null, index)
        } else {
            MutableFragment()
        }
    }

    /**
     * Subscript access to a Fragment object by key.
     *
     * @param key The key.
     */
    override fun get(key: String): MutableFragment {
        val parent = when {
            this.key != null -> dictParent.getValue(this.key)
            index != null -> arrayParent.getValue(index)
            else -> null
        }
        return if (parent is MutableDictionaryInterface) {
            MutableFragment(parent, key, null)
        } else {
            MutableFragment()
        }
    }
}
