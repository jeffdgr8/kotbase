package kotbase

/**
 * **ENTERPRISE EDITION API**
 *
 * Message endpoint.
 */
public expect class MessageEndpoint

/**
 * Initializes a MessageEndpoint object.
 *
 * @param uid          the unique identifier of the endpoint
 * @param target       an optional arbitrary object that represents the endpoint
 * @param protocolType the data transportation protocol
 * @param delegate     the delegate for creating MessageEndpointConnection objects
 */
constructor(
    uid: String,
    target: Any?,
    protocolType: ProtocolType,
    delegate: MessageEndpointDelegate
) : Endpoint {

    /**
     * The unique identifier of the endpoint.
     */
    public val uid: String

    /**
     * The target object which is an arbitrary object that represents the endpoint.
     */
    public val target: Any?

    /**
     * Gets the data transportation protocol of the endpoint.
     *
     * @return the data transportation protocol
     */
    public val protocolType: ProtocolType

    /**
     * Gets the delegate object used for creating MessageEndpointConnection objects.
     *
     * @return the delegate object.
     */
    public val delegate: MessageEndpointDelegate
}
