package kotbase

public actual class ClientCertificateAuthenticator
actual constructor(
    public actual val identity: TLSIdentity
) : Authenticator(urlEndpointListenerUnsupported())
