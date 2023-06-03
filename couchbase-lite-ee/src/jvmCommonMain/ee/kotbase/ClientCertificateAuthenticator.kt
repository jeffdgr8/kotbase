package kotbase

import com.couchbase.lite.ClientCertificateAuthenticator
import kotbase.base.DelegatedClass

public actual class ClientCertificateAuthenticator
private constructor(
    override val actual: com.couchbase.lite.ClientCertificateAuthenticator,
    public actual val identity: TLSIdentity
) : DelegatedClass<ClientCertificateAuthenticator>(actual), Authenticator {

    public actual constructor(identity: TLSIdentity) : this(
        com.couchbase.lite.ClientCertificateAuthenticator(identity.actual),
        identity
    )
}
