package com.couchbase.lite.kmm

/**
 * Gets the log controller for Couchbase Lite, which stores the
 * three available logging methods:  console (logcat), file, and
 * custom.
 */
public expect class Log {

    /**
     * The logger that writes to the system console
     */
    public val console: ConsoleLogger

    /**
     * The logger that writes to log files
     */
    public val file: FileLogger

    /**
     * The custom logger that was registered by the
     * application (if any)
     */
    public var custom: Logger?
}
