package kotbase

/**
 * A Listener Certificate Authenticator
 * Certificate base authentication and authorization.
 */
public expect class ListenerCertificateAuthenticator : ListenerAuthenticator {

    /**
     * Create an authenticator that allows clients whose certificate chains can be verified using (only)
     * on of the certs in the passed list.  OS-bundled certs are ignored.
     *
     * @param rootCerts root certificates used to verify client certificate chains.
     */
    public constructor(rootCerts: List<ByteArray>)

    /**
     * Create an authenticator that delegates all responsibility for authentication and authorization
     * to the passed delegate.  See [ListenerCertificateAuthenticatorDelegate].
     *
     * @param delegate an authenticator
     */
    public constructor(delegate: ListenerCertificateAuthenticatorDelegate)
}
