package com.couchbase.lite.kmm

/**
 * The logging interface for Couchbase Lite.  If an application wants
 * to receive log messages to an arbitrary endpoint, then it can by
 * implementing this interface.
 *
 * !!! There is, currently, no way to tell when the log level changes
 * for a custom logger.  Setting a more verbose log level will not
 * have any effect until the logger is polled for its level.
 */
public interface Logger {

    /**
     * Gets the level that will be logged via this logger.
     *
     * @return The maximum level to log
     */
    public val level: LogLevel

    /**
     * Performs the actual logging logic
     *
     * @param level   The level of the message to log
     * @param domain  The domain of the message to log
     * @param message The content of the message to log
     */
    public fun log(level: LogLevel, domain: LogDomain, message: String)
}
