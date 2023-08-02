package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ClientCertificateAuthenticator as CBLClientCertificateAuthenticator

public actual class ClientCertificateAuthenticator
private constructor(
    override val actual: CBLClientCertificateAuthenticator,
    public actual val identity: TLSIdentity
) : DelegatedClass<CBLClientCertificateAuthenticator>(actual), Authenticator {

    public actual constructor(identity: TLSIdentity) : this(
        CBLClientCertificateAuthenticator(identity.actual),
        identity
    )
}
