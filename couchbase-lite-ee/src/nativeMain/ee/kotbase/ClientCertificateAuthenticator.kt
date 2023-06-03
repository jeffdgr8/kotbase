package kotbase

import cnames.structs.CBLAuthenticator
import kotlinx.cinterop.CPointer

public actual class ClientCertificateAuthenticator
actual constructor(identity: TLSIdentity) : Authenticator {

    init {
        urlEndpointListenerUnsupported()
    }

    override val actual: CPointer<CBLAuthenticator>

    public actual val identity: TLSIdentity
}
