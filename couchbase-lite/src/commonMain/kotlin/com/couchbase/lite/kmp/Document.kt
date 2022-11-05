package com.couchbase.lite.kmp

import kotlinx.datetime.Instant

/**
 * Readonly version of the Document.
 */
public expect open class Document : Iterable<String> {

    /**
     * The document's ID.
     */
    public val id: String

    /**
     * The document's revision id.
     * The revision id in the Document class is a constant while the revision id in the MutableDocument
     * class is not. A newly created Document will have a null revision id. The revision id in
     * a MutableDocument will be updated on save. The revision id format is opaque, which means its format
     * has no meaning and shouldn't be parsed to get information.
     */
    public val revisionID: String?

    /**
     * The sequence number of the document in the database.
     * The sequence number indicates how recently the document has been changed.  Every time a document
     * is updated, the database assigns it the next sequential sequence number.  Thus, when a document's
     * sequence number changes it means that the document been update (on-disk).  If one document's sequence
     * is different than another's, the document with the larger sequence number was changed more recently.
     * Sequence numbers are not available for documents obtained from a replication filter.  This method
     * will always return 0 for such documents.
     */
    public val sequence: Long

    /**
     * Return a mutable copy of the document
     *
     * @return the MutableDocument instance
     */
    public open fun toMutable(): MutableDocument

    /**
     * The number of the entries in the dictionary.
     */
    public val count: Int

    /**
     * A List containing all keys, or an empty List if the document has no properties.
     */
    public val keys: List<String>

    /**
     * Gets a property's value as an object. The object types are Blob, Array,
     * Dictionary, Number, or String based on the underlying data type; or nil if the
     * property value is null or the property doesn't exist.
     *
     * @param key the key.
     * @return the object value or null.
     */
    public fun getValue(key: String): Any?

    /**
     * Gets a property's value as a String.
     * Returns null if the value doesn't exist, or its value is not a String.
     *
     * @param key the key
     * @return the String or null.
     */
    public fun getString(key: String): String?

    /**
     * Gets a property's value as a Number.
     * Returns null if the value doesn't exist, or its value is not a Number.
     *
     * @param key the key
     * @return the Number or nil.
     */
    public fun getNumber(key: String): Number?

    /**
     * Gets a property's value as an int.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the int value.
     */
    public fun getInt(key: String): Int

    /**
     * Gets a property's value as an long.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the long value.
     */
    public fun getLong(key: String): Long

    /**
     * Gets a property's value as an float.
     * Integers will be converted to float. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the float value.
     */
    public fun getFloat(key: String): Float

    /**
     * Gets a property's value as an double.
     * Integers will be converted to double. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the property doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the double value.
     */
    public fun getDouble(key: String): Double

    /**
     * Gets a property's value as a boolean. Returns true if the value exists, and is either `true`
     * or a nonzero number.
     *
     * @param key the key
     * @return the boolean value.
     */
    public fun getBoolean(key: String): Boolean

    /**
     * Gets a property's value as a Blob.
     * Returns null if the value doesn't exist, or its value is not a Blob.
     *
     * @param key the key
     * @return the Blob value or null.
     */
    public fun getBlob(key: String): Blob?

    /**
     * Gets a property's value as a Date.
     * JSON does not directly support dates, so the actual property value must be a string, which is
     * then parsed according to the ISO-8601 date format (the default used in JSON.)
     * Returns null if the value doesn't exist, is not a string, or is not parsable as a date.
     * NOTE: This is not a generic date parser! It only recognizes the ISO-8601 format, with or
     * without milliseconds.
     *
     * @param key the key
     * @return the Date value or null.
     */
    public fun getDate(key: String): Instant?

    /**
     * Get a property's value as a Array, which is a mapping object of an array value.
     * Returns null if the property doesn't exists, or its value is not an Array.
     *
     * @param key the key
     * @return The Array object or null.
     */
    public open fun getArray(key: String): Array?

    /**
     * Get a property's value as a Dictionary, which is a mapping object of
     * a Dictionary value.
     * Returns null if the property doesn't exists, or its value is not a Dictionary.
     *
     * @param key the key
     * @return The Dictionary object or null.
     */
    public open fun getDictionary(key: String): Dictionary?

    /**
     * Gets content of the current object as an Map. The values contained in the returned
     * Map object are all JSON based values.
     *
     * @return the Map object representing the content of the current object in the JSON format.
     */
    public fun toMap(): Map<String, Any?>

    public fun toJSON(): String?

    /**
     * Tests whether a property exists or not.
     * This can be less expensive than getValue(String),
     * because it does not have to allocate an Object for the property value.
     *
     * @param key the key
     * @return the boolean value representing whether a property exists or not.
     */
    public operator fun contains(key: String): Boolean

    /**
     * Gets an iterator over the keys of the document's properties
     *
     * @return The key iterator
     */
    override operator fun iterator(): Iterator<String>
}

/**
 * Subscript access to a Fragment object by key.
 */
public operator fun Document.get(key: String): Fragment =
    Fragment(this, key)
