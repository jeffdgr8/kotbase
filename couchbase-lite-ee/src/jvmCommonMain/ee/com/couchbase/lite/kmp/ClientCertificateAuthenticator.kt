package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class ClientCertificateAuthenticator
private constructor(
    override val actual: com.couchbase.lite.ClientCertificateAuthenticator,
    public actual val identity: TLSIdentity
) : DelegatedClass<com.couchbase.lite.ClientCertificateAuthenticator>(actual), Authenticator {

    public actual constructor(identity: TLSIdentity) : this(
        com.couchbase.lite.ClientCertificateAuthenticator(identity.actual),
        identity
    )
}
