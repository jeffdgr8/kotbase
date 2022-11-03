package com.couchbase.lite.kmp

/**
 * A change event posted by MessageEndpointListener.
 */
public expect class MessageEndpointListenerChange {

    /**
     * Return connection
     *
     * @return the connection
     */
    public val connection: MessageEndpointConnection

    /**
     * Return replicator status
     *
     * @return status
     */
    public val status: ReplicatorStatus
}
