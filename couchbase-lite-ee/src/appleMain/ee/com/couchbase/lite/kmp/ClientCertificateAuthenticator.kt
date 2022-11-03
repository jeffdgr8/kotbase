package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLClientCertificateAuthenticator
import com.udobny.kmp.DelegatedClass

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
