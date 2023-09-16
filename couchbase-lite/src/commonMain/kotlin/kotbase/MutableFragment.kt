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

    internal constructor(parent: MutableDocument, key: String) :
            this(parent, key, null)

    internal constructor(parent: MutableDictionary, key: String) :
            this(parent, key, null)

    internal constructor(parent: MutableArray, index: Int) :
            this(parent, null, index)

    internal constructor() : this(null, null, null)

    /**
     * Gets the value from or sets the value to the fragment object.
     */
    override var value: Any?
        get() = super.value
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setValue(key!!, value)
                is MutableDictionary -> parent.setValue(key!!, value)
                is MutableArray -> parent.setValue(index!!, value)
            }
        }

    /**
     * Gets the value as string or sets the string value to the fragment object.
     */
    override var string: String?
        get() = super.string
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setString(key!!, value)
                is MutableDictionary -> parent.setString(key!!, value)
                is MutableArray -> parent.setString(index!!, value)
            }
        }

    /**
     * Gets the value as a number or sets the number value to the fragment object.
     */
    override var number: Number?
        get() = super.number
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setNumber(key!!, value)
                is MutableDictionary -> parent.setNumber(key!!, value)
                is MutableArray -> parent.setNumber(index!!, value)
            }
        }

    /**
     * Gets the value as integer or sets the integer value to the fragment object.
     */
    override var int: Int
        get() = super.int
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setInt(key!!, value)
                is MutableDictionary -> parent.setInt(key!!, value)
                is MutableArray -> parent.setInt(index!!, value)
            }
        }

    /**
     * Gets the value as long or sets the long value to the fragment object.
     */
    override var long: Long
        get() = super.long
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setLong(key!!, value)
                is MutableDictionary -> parent.setLong(key!!, value)
                is MutableArray -> parent.setLong(index!!, value)
            }
        }

    /**
     * Gets the value as float or sets the float value to the fragment object.
     */
    override var float: Float
        get() = super.float
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setFloat(key!!, value)
                is MutableDictionary -> parent.setFloat(key!!, value)
                is MutableArray -> parent.setFloat(index!!, value)
            }
        }

    /**
     * Gets the value as double or sets the double value to the fragment object.
     */
    override var double: Double
        get() = super.double
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setDouble(key!!, value)
                is MutableDictionary -> parent.setDouble(key!!, value)
                is MutableArray -> parent.setDouble(index!!, value)
            }
        }

    /**
     * Gets the value as boolean or sets the boolean value to the fragment object.
     */
    override var boolean: Boolean
        get() = super.boolean
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setBoolean(key!!, value)
                is MutableDictionary -> parent.setBoolean(key!!, value)
                is MutableArray -> parent.setBoolean(index!!, value)
            }
        }

    /**
     * Gets the value as blob or sets the blob value to the fragment object.
     */
    override var blob: Blob?
        get() = super.blob
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setBlob(key!!, value)
                is MutableDictionary -> parent.setBlob(key!!, value)
                is MutableArray -> parent.setBlob(index!!, value)
            }
        }

    /**
     * Gets the value as date or sets the date value to the fragment object.
     */
    override var date: Instant?
        get() = super.date
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setDate(key!!, value)
                is MutableDictionary -> parent.setDate(key!!, value)
                is MutableArray -> parent.setDate(index!!, value)
            }
        }

    /**
     * Get the value as a MutableArray object, a mapping object of an array
     * value. Returns null if the value is null, or the value is not an array.
     */
    override var array: MutableArray?
        get() = when (parent) {
            is MutableDocument -> parent.getArray(key!!)
            is MutableDictionary -> parent.getArray(key!!)
            is MutableArray -> parent.getArray(index!!)
            else -> null
        }
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setArray(key!!, value)
                is MutableDictionary -> parent.setArray(key!!, value)
                is MutableArray -> parent.setArray(index!!, value)
            }
        }

    /**
     * Get the value as a MutableDictionary object, a mapping object of a dictionary
     * value. Returns null if the value is null, or the value is not a dictionary.
     */
    override var dictionary: MutableDictionary?
        get() = when (parent) {
            is MutableDocument -> parent.getDictionary(key!!)
            is MutableDictionary -> parent.getDictionary(key!!)
            is MutableArray -> parent.getDictionary(index!!)
            else -> null
        }
        set(value) {
            when (parent) {
                is MutableDocument -> parent.setDictionary(key!!, value)
                is MutableDictionary -> parent.setDictionary(key!!, value)
                is MutableArray -> parent.setDictionary(index!!, value)
            }
        }

    /**
     * Subscript access to a Fragment object by index.
     *
     * @param index The index. If the index value exceeds the bounds of the array,
     * the MutableFragment object will represent a nil value.
     */
    override fun get(index: Int): MutableFragment {
        val parent = when (parent) {
            is Document -> parent.getValue(key!!)
            is Dictionary -> parent.getValue(key!!)
            is Array -> parent.getValue(this.index!!)
            else -> null
        }
        return if (parent is Array && index in 0 until parent.count) {
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
        val parent = when (parent) {
            is Document -> parent.getValue(this.key!!)
            is Dictionary -> parent.getValue(this.key!!)
            is Array -> parent.getValue(index!!)
            else -> null
        }
        return if (parent is Dictionary) {
            MutableFragment(parent, key, null)
        } else {
            MutableFragment()
        }
    }
}
