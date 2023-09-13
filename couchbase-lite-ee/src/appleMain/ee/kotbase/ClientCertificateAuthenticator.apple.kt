package kotbase

import cocoapods.CouchbaseLite.CBLClientCertificateAuthenticator

public actual class ClientCertificateAuthenticator
private constructor(
    internal val actual: CBLClientCertificateAuthenticator,
    public actual val identity: TLSIdentity
) : Authenticator(actual) {

    public actual constructor(identity: TLSIdentity) : this(
        CBLClientCertificateAuthenticator(identity.actual),
        identity
    )
}
