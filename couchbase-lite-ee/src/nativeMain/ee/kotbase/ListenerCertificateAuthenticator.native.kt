package kotbase

public actual class ListenerCertificateAuthenticator : ListenerAuthenticator {

    init {
        urlEndpointListenerUnsupported()
    }

    public actual constructor(rootCerts: List<ByteArray>)

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate)
}
