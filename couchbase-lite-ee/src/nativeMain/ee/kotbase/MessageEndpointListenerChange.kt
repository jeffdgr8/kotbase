package kotbase

public actual class MessageEndpointListenerChange {

    init {
        messageEndpointUnsupported()
    }

    public actual val connection: MessageEndpointConnection

    public actual val status: ReplicatorStatus
}
