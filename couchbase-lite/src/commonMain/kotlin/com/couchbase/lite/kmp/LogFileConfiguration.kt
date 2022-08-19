package com.couchbase.lite.kmp

/**
 * A class that describes the file configuration for the [FileLogger] class.
 * These options must be set atomically so they won't take effect unless a new
 * configuration object is set on the logger.  Attempting to modify an in-use
 * configuration object will result in an exception being thrown.
 */
public expect class LogFileConfiguration {

    /**
     * Constructs a file configuration object with the given directory
     *
     * @param directory The directory that the logs will be written to
     */
    public constructor(directory: String)

    /**
     * Constructs a file configuration object based on another one so
     * that it may be modified
     *
     * @param config The other configuration to copy settings from
     */
    public constructor(config: LogFileConfiguration)

    /**
     * Constructs a file configuration object based on another one but changing
     * the directory
     *
     * @param directory The directory that the logs will be written to
     * @param config    The other configuration to copy settings from
     */
    public constructor(directory: String, config: LogFileConfiguration?)

    /**
     * Sets whether or not to log in plaintext.  The default is
     * to log in a binary encoded format that is more CPU and I/O friendly
     * and enabling plaintext is not recommended in production.
     *
     * @param usePlaintext Whether or not to log in plaintext
     * @return The self object
     */
    public fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration

    /**
     * The number of rotated logs that are saved (i.e.
     * if the value is 1, then 2 logs will be present:  the 'current'
     * and the 'rotated')
     */
    public var maxRotateCount: Int

    /**
     * Sets the number of rotated logs that are saved (i.e.
     * if the value is 1, then 2 logs will be present:  the 'current'
     * and the 'rotated')
     *
     * @param maxRotateCount The number of rotated logs to be saved
     * @return The self object
     */
    public fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration

    /**
     * The max size of the log file in bytes.  If a log file
     * passes this size then a new log file will be started.  This
     * number is a best effort and the actual size may go over slightly.
     */
    public var maxSize: Long

    /**
     * Sets the max size of the log file in bytes.  If a log file
     * passes this size then a new log file will be started.  This
     * number is a best effort and the actual size may go over slightly.
     *
     * @param maxSize The max size of the log file in bytes
     * @return The self object
     */
    public fun setMaxSize(maxSize: Long): LogFileConfiguration

    /**
     * Whether or not CBL is logging in plaintext.  The default is
     * to log in a binary encoded format that is more CPU and I/O friendly
     * and enabling plaintext is not recommended in production.
     */
    public var usesPlaintext: Boolean

    /**
     * The directory that the logs files are stored in.
     */
    public val directory: String
}
