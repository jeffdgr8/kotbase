package kotbase

/**
 * **ENTERPRISE EDITION API**
 *
 * The interface implemented by the application using a custom transportation
 * method to exchange replication data between peers.
 */
public interface MessageEndpointConnection {

    /**
     * Called to open a remote connection to the other peer when the replicator
     * starts or when the MessageEndpointListener accepts the connection.
     * When the remote connection is established, call the completion block to
     * acknowledge the completion.
     *
     * @param connection the replicator connection
     * @param completion the completion callback
     */
    public fun open(connection: ReplicatorConnection, completion: MessagingCompletion)

    /**
     * Called to close the remote connection with the other peer when the
     * replicator stops or when the MessageEndpointListener closes the connection.
     * When the remote connection is closed, call the completion block to acknowledge
     * the completion.
     *
     * @param completion the completion callback
     */
    public fun close(error: Exception?, completion: MessagingCloseCompletion)

    /**
     * Called to send the replication data to the other peer. When the replication
     * data is sent, call the completion block to acknowledge the completion.
     *
     * @param message    the message
     * @param completion the completion callback
     */
    public fun send(message: Message, completion: MessagingCompletion)
}
