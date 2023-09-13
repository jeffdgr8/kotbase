package kotbase

import com.couchbase.lite.ClientCertificateAuthenticator as CBLClientCertificateAuthenticator

public actual class ClientCertificateAuthenticator
private constructor(
    actual: CBLClientCertificateAuthenticator,
    public actual val identity: TLSIdentity
) : Authenticator(actual) {

    public actual constructor(identity: TLSIdentity) : this(
        CBLClientCertificateAuthenticator(identity.actual),
        identity
    )
}

internal val ClientCertificateAuthenticator.actual: CBLClientCertificateAuthenticator
    get() = platformState.actual as CBLClientCertificateAuthenticator
