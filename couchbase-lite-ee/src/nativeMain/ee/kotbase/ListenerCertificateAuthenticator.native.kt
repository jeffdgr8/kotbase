package kotbase

public actual class ListenerCertificateAuthenticator : ListenerAuthenticator {

    public actual constructor(rootCerts: List<ByteArray>)

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate)
}
