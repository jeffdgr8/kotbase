package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * The replicator connection used by the application to tell the replicator to
 * consume the data received from the other peer or to close the connection.
 */
public interface ReplicatorConnection {

    /**
     * Tells the replicator to close the current replicator connection. In return,
     * the replicator will call the MessageEndpointConnection's close(error, completion)
     * to acknowledge the closed connection.
     *
     * @param error the error if any
     */
    public fun close(error: MessagingError?)

    /**
     * Tells the replicator to consume the data received from the other peer.
     *
     * @param message the message
     */
    public fun receive(message: Message)
}
