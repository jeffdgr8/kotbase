package kotbase

/**
 * **ENTERPRISE EDITION API**
 */
public expect class URLEndpointListener

/**
 * Create a URLEndpointListener with the passed configuration.
 *
 * @param config the listener configuration.
 */
constructor(config: URLEndpointListenerConfiguration) {

    /**
     * The listener's configuration (read only).
     */
    public val config: URLEndpointListenerConfiguration

    /**
     * Get the listener's port.
     * This method will return null except between the time
     * the listener is started and the time it is stopped.
     *
     * When a listener is configured with the port number 0, the return value from this function will
     * give the port at which the listener is actually listening.
     *
     * @return the listener's port, or null.
     */
    public val port: Int?

    /**
     * Get the list of URIs for the listener.
     *
     * @return a list of listener URIs.
     */
    public val urls: List<String>

    /**
     * The listener status.
     */
    public val status: ConnectionStatus?

    /**
     * The TLS identity used by the listener.
     */
    public val tlsIdentity: TLSIdentity?

    /**
     * Start the listener.
     */
    @Throws(CouchbaseLiteException::class)
    public fun start()

    /**
     * Stop the listener.
     */
    public fun stop()
}
