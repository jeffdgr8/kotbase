package com.couchbase.lite.kmm

/**
 * Log level.
 */
public expect enum class LogLevel {

    /**
     * Debugging information.
     */
    DEBUG,

    /**
     * Low level logging.
     */
    VERBOSE,

    /**
     * Essential state info and client errors that are recoverable
     */
    INFO,

    /**
     * Internal errors that are recoverable; client errors that may not be recoverable
     */
    WARNING,

    /**
     * Internal errors that are unrecoverable
     */
    ERROR,

    /**
     * Disabling log messages of a given log domain.
     */
    NONE
}
