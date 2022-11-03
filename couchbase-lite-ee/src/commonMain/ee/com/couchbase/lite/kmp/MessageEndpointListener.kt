package com.couchbase.lite.kmp

/**
 * MessageEndpointListener to serve incoming message endpoint connection.
 */
public expect class MessageEndpointListener(config: MessageEndpointListenerConfiguration) {

    /**
     * Accept a new connection.
     *
     * @param connection new incoming connection
     */
    public fun accept(connection: MessageEndpointConnection)

    /**
     * Close the given connection.
     *
     * @param connection the connection to be closed
     */
    public fun close(connection: MessageEndpointConnection)

    /**
     * Close all connections active at the time of the call.
     */
    public fun closeAll()

    /**
     * Add a change listener.
     *
     * @param listener the listener
     * @return listener identifier
     */
    public fun addChangeListener(listener: MessageEndpointListenerChangeListener): ListenerToken

    // TODO:
    ///**
    // * Add a change listener with the given dispatch queue.
    // *
    // * @param queue    the executor on which the listener will run
    // * @param listener the listener
    // * @return listener identifier
    // */
    //public fun addChangeListener(queue: Executor?, listener: MessageEndpointListenerChangeListener): ListenerToken

    /**
     * Remove a change listener.
     *
     * @param token identifier for the listener to be removed
     */
    public fun removeChangeListener(token: ListenerToken)
}
