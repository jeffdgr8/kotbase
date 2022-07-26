package com.couchbase.lite.kmm

/**
 * A logger for writing to a file in the application's storage so
 * that log messages can persist durably after the application has
 * stopped or encountered a problem.  Each log level is written to
 * a separate file.
 * Threading policy: This class is certain to be used from multiple
 * threads.  As long as it is thread safe, the various race conditions
 * are unlikely and the penalties very small.  "Volatile" ensures
 * the thread safety and the several races are tolerable.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class FileLogger : Logger {

    /**
     * The maximum logging level that will be written to the logging files.
     */
    override var level: LogLevel

    /**
     * The configuration currently in use by the file logger.
     * Note that once a configuration has been installed in a logger,
     * the configuration is read-only and can no longer be modified.
     * An attempt to modify the configuration returned by this method will cause an exception.
     */
    public var config: LogFileConfiguration?
}
