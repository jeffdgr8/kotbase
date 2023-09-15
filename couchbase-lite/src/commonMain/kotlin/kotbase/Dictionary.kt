package kotbase

import kotbase.internal.DbContext
import kotlinx.datetime.Instant
import kotlin.reflect.safeCast

internal expect class DictionaryPlatformState

/**
 * Dictionary provides readonly access to dictionary data.
 */
public expect open class Dictionary : Iterable<String> {

    internal val platformState: DictionaryPlatformState

    internal val collectionMap: MutableMap<String, Any>

    internal open var dbContext: DbContext?

    /**
     * Return a mutable copy of the dictionary
     *
     * @return the MutableDictionary instance
     */
    public fun toMutable(): MutableDictionary

    /**
     * The number of the entries in the dictionary.
     */
    public val count: Int

    /**
     * A List containing all keys, or an empty List if the dictionary has no properties.
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
     * Gets a property's value as a String. Returns null if the value doesn't exist, or its value is not a String.
     *
     * @param key the key
     * @return the String or null.
     */
    public fun getString(key: String): String?

    /**
     * Gets a property's value as a Number. Returns null if the value doesn't exist, or its value is not a Number.
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
     * Gets a property's value as a long.
     * Floating point values will be rounded. The value `true` is returned as 1, `false` as 0.
     * Returns 0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the long value.
     */
    public fun getLong(key: String): Long

    /**
     * Gets a property's value as a float.
     * Integers will be converted to float. The value `true` is returned as 1.0, `false` as 0.0.
     * Returns 0.0 if the value doesn't exist or does not have a numeric value.
     *
     * @param key the key
     * @return the float value.
     */
    public fun getFloat(key: String): Float

    /**
     * Gets a property's value as a double.
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
     * Get a property's value as an Array, which is a mapping object of an array value.
     * Returns null if the property doesn't exist, or its value is not an array.
     *
     * @param key the key.
     * @return the Array object.
     */
    public open fun getArray(key: String): Array?

    /**
     * Get a property's value as a Dictionary, which is a mapping object of a dictionary value.
     * Returns null if the property doesn't exist, or its value is not a dictionary.
     *
     * @param key the key.
     * @return the Dictionary object or null if the key doesn't exist.
     */
    public open fun getDictionary(key: String): Dictionary?

    /**
     * Gets content of the current object as a Map. The values contained in the returned
     * Map object are all JSON based values.
     *
     * @return the Map object representing the content of the current object in the JSON format.
     */
    public fun toMap(): Map<String, Any?>

    public fun toJSON(): String

    /**
     * Tests whether a property exists or not.
     * This can be less expensive than getValue(String), because it does not have to allocate an Object for the
     * property value.
     *
     * @param key the key
     * @return the boolean value representing whether a property exists or not.
     */
    public operator fun contains(key: String): Boolean
}

/**
 * Subscript access to a Fragment object by key.
 *
 * @param key The key.
 */
public operator fun Dictionary.get(key: String): Fragment =
    Fragment(this, key)

internal inline fun <reified T : Any> Dictionary.getInternalCollection(key: String): T? =
    T::class.safeCast(collectionMap[key])
