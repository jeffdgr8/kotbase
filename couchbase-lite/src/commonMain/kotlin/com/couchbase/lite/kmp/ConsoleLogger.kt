@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * A class that sends log messages to the system log, available via 'logcat' on Android.
 */
public expect class ConsoleLogger : Logger {

    /**
     * The overall logging level that will be written to the console log.
     * The lowest (most verbose) level to include in the logs
     */
    override var level: LogLevel

    /**
     * The domains that will be considered for writing to the console log.
     */
    public var domains: Set<LogDomain>

    /**
     * Sets the domains that will be considered for writing to the console log.
     *
     * @param domains The domains to make active (vararg)
     */
    public fun setDomains(vararg domains: LogDomain)
}
