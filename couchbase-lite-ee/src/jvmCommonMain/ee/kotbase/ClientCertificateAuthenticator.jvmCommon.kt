package kotbase

import com.couchbase.lite.ClientCertificateAuthenticator as CBLClientCertificateAuthenticator

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
