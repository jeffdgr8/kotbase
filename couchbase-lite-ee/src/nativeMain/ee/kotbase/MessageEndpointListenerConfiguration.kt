package kotbase

public actual class MessageEndpointListenerConfiguration
actual constructor(
    public actual val database: Database,
    public actual val protocolType: ProtocolType
) {

    init {
        messageEndpointUnsupported()
    }
}
