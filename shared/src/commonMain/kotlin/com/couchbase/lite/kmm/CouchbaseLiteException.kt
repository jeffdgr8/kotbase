package com.couchbase.lite.kmm

/**
 * A CouchbaseLiteException gets raised whenever a Couchbase Lite faces errors.
 */
public expect class CouchbaseLiteException : Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public constructor(message: String)

    /**
     * Constructs a new exception with the specified cause
     *
     * @param cause the cause
     */
    public constructor(message: String, cause: Exception)

    /**
     * Constructs a new exception with the specified detail message, error domain and error code
     *
     * @param message the detail message
     * @param domain  the error domain
     * @param code    the error code
     */
    public constructor(message: String, domain: String, code: Int)

    /**
     * Constructs a new exception with the specified error domain, error code and the specified cause
     *
     * @param message the detail message
     * @param cause   the cause
     * @param domain  the error domain
     * @param code    the error code
     */
    public constructor(message: String, cause: Exception, domain: String, code: Int)

    /**
     * Access the error domain for this error.
     *
     * @return The numerical domain code for this error.
     */
    public fun getDomain(): String

    /**
     * Access the error code for this error.
     *
     * @return The numerical error code for this error.
     */
    public fun getCode(): Int

    public fun getInfo(): Map<String, Any?>?
}
