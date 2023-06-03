package kotbase

public actual class ListenerPasswordAuthenticator
actual constructor(delegate: ListenerPasswordAuthenticatorDelegate) : ListenerAuthenticator {

    init {
        urlEndpointListenerUnsupported()
    }
}
