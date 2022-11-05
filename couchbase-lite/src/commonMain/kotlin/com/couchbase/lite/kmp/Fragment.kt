package com.couchbase.lite.kmp

import kotlinx.datetime.Instant

/**
 * Fragment provides readonly access to data value. Fragment also provides subscript access
 * by either key or index to the nested values which are wrapped by Fragment objects.
 */
public open class Fragment
private constructor(
    protected val parent: Any,
    protected val key: String?,
    protected val index: Int?
) {

    internal constructor(parent: Document, key: String) :
            this(parent, key, null)

    internal constructor(parent: Dictionary, key: String) :
            this(parent, key, null)

    internal constructor(parent: Array, index: Int) :
            this(parent, null, index)

    /**
     * Gets the fragment value. The value types are Blob, Array, Dictionary, Number,
     * or String based on the underlying data type; or null if the value is null.
     */
    public val value: Any?
        get() {
            return when (parent) {
                is Document -> parent.getValue(key!!)
                is Dictionary -> parent.getValue(key!!)
                is Array -> parent.getValue(index!!)
                else -> null
            }
        }

    /**
     * Gets the value as a string. Returns null if the value is null, or the value is not a string.
     */
    public val string: String?
        get() {
            return when (parent) {
                is Document -> parent.getString(key!!)
                is Dictionary -> parent.getString(key!!)
                is Array -> parent.getString(index!!)
                else -> null
            }
        }

    /**
     * Gets the value as a Number. Returns null if the value
     * doesn't exist, or its value is not a Number.
     */
    public val number: Number?
        get() {
            return when (parent) {
                is Document -> parent.getNumber(key!!)
                is Dictionary -> parent.getNumber(key!!)
                is Array -> parent.getNumber(index!!)
                else -> null
            }
        }

    /**
     * Gets the value as an int. Floating point values will be rounded. The value true is
     * returned as 1, false as 0. Returns 0 if the value is null or is not a numeric value.
     */
    public val int: Int
        get() {
            return when (parent) {
                is Document -> parent.getInt(key!!)
                is Dictionary -> parent.getInt(key!!)
                is Array -> parent.getInt(index!!)
                else -> 0
            }
        }

    /**
     * Gets the value as a long. Floating point values will be rounded. The value true is
     * returned as 1, false as 0. Returns 0 if the value is null or is not a numeric value.
     */
    public val long: Long
        get() {
            return when (parent) {
                is Document -> parent.getLong(key!!)
                is Dictionary -> parent.getLong(key!!)
                is Array -> parent.getLong(index!!)
                else -> 0
            }
        }

    /**
     * Gets the value as a float. Integers will be converted to float. The value true is
     * returned as 1.0, false as 0.0. Returns 0.0 if the value is null or is not a numeric value.
     */
    public val float: Float
        get() {
            return when (parent) {
                is Document -> parent.getFloat(key!!)
                is Dictionary -> parent.getFloat(key!!)
                is Array -> parent.getFloat(index!!)
                else -> 0F
            }
        }

    /**
     * Gets the value as a double. Integers will be converted to double. The value true is
     * returned as 1.0, false as 0.0. Returns 0.0 if the value is null or is not a numeric value.
     */
    public val double: Double
        get() {
            return when (parent) {
                is Document -> parent.getDouble(key!!)
                is Dictionary -> parent.getDouble(key!!)
                is Array -> parent.getDouble(index!!)
                else -> 0.0
            }
        }

    /**
     * Gets the value as a boolean. Returns true if the value
     * is not null, and is either true or a nonzero number.
     */
    public val boolean: Boolean
        get() {
            return when (parent) {
                is Document -> parent.getBoolean(key!!)
                is Dictionary -> parent.getBoolean(key!!)
                is Array -> parent.getBoolean(index!!)
                else -> false
            }
        }

    /**
     * Get the value as a Blob. Returns null if the value is null, or the value is not a Blob.
     */
    public val blob: Blob?
        get() {
            return when (parent) {
                is Document -> parent.getBlob(key!!)
                is Dictionary -> parent.getBlob(key!!)
                is Array -> parent.getBlob(index!!)
                else -> null
            }
        }

    /**
     * Gets the value as a date. JSON does not directly support dates, so the actual property value
     * must be a string, which is then parsed according to the ISO-8601 date format (the default
     * used in JSON.) Returns null if the value is null, is not a string, or is not parseable as a
     * date. NOTE: This is not a generic date parser! It only recognizes the ISO-8601 format, with
     * or without milliseconds.
     */
    public val date: Instant?
        get() {
            return when (parent) {
                is Document -> parent.getDate(key!!)
                is Dictionary -> parent.getDate(key!!)
                is Array -> parent.getDate(index!!)
                else -> null
            }
        }

    /**
     * Get the value as an Array, a mapping object of an array value.
     * Returns null if the value is null, or the value is not an array.
     */
    public val array: Array?
        get() {
            return when (parent) {
                is Document -> parent.getArray(key!!)
                is Dictionary -> parent.getArray(key!!)
                is Array -> parent.getArray(index!!)
                else -> null
            }
        }

    /**
     * Get a property’s value as a Dictionary, a mapping object of a dictionary
     * value. Returns null if the value is null, or the value is not a dictionary.
     */
    public val dictionary: Dictionary?
        get() {
            return when (parent) {
                is Document -> parent.getDictionary(key!!)
                is Dictionary -> parent.getDictionary(key!!)
                is Array -> parent.getDictionary(index!!)
                else -> null
            }
        }

    /**
     * Checks whether the value held by the fragment object exists or is null value or not.
     */
    public val exists: Boolean
        get() = value != null
}
