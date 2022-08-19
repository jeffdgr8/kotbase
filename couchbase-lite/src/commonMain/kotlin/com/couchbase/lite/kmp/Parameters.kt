package com.couchbase.lite.kmp

import kotlinx.datetime.Instant

/**
 * A Parameters object used for setting values to the query parameters defined in the query.
 */
public expect class Parameters(parameters: Parameters? = null) {

    /**
     * Gets a parameter's value.
     *
     * @param name The parameter name.
     * @return The parameter value.
     */
    public fun getValue(name: String): Any?

    /**
     * Set an String value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The String value.
     * @return The self object.
     */
    public fun setString(name: String, value: String?): Parameters

    /**
     * Set an Number value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The Number value.
     * @return The self object.
     */
    public fun setNumber(name: String, value: Number?): Parameters

    /**
     * Set an int value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The int value.
     * @return The self object.
     */
    public fun setInt(name: String, value: Int): Parameters

    /**
     * Set an long value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The long value.
     * @return The self object.
     */
    public fun setLong(name: String, value: Long): Parameters

    /**
     * Set a float value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The float value.
     * @return The self object.
     */
    public fun setFloat(name: String, value: Float): Parameters

    /**
     * Set a double value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The double value.
     * @return The self object.
     */
    public fun setDouble(name: String, value: Double): Parameters

    /**
     * Set a boolean value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The boolean value.
     * @return The self object.
     */
    public fun setBoolean(name: String, value: Boolean): Parameters

    /**
     * Set a date value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The date value.
     * @return The self object.
     */
    public fun setDate(name: String, value: Instant?): Parameters

    /**
     * Set the Blob value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The Blob value.
     * @return The self object.
     */
    public fun setBlob(name: String, value: Blob?): Parameters

    /**
     * Set the Dictionary value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The Dictionary value.
     * @return The self object.
     */
    public fun setDictionary(name: String, value: Dictionary?): Parameters

    /**
     * Set the Array value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The Array value.
     * @return The self object.
     */
    public fun setArray(name: String, value: Array?): Parameters

    /**
     * Set a value to the query parameter referenced by the given name. A query parameter
     * is defined by using the Expression's parameter(String name) function.
     *
     * @param name  The parameter name.
     * @param value The value.
     * @return The self object.
     */
    public fun setValue(name: String, value: Any?): Parameters
}
