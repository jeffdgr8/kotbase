package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * The messaging error.
 */
public expect class MessagingError

/**
 * Creates a MessagingError with the given error and recoverable flag identifying
 * if the error is recoverable or not. The replicator uses recoverable
 * flag to determine whether the replication should be retried or stopped as the error
 * is non-recoverable.
 *
 * @param error       the error
 * @param recoverable the recoverable flag
 */
constructor(error: Exception, recoverable: Boolean) {

    /**
     * Is the error recoverable?
     *
     * The recoverable flag identifying whether the error is recoverable or not
     */
    public val isRecoverable: Boolean

    /**
     * The error object.
     */
    public val error: Exception
}
