package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import sun.security.x509.X509CertImpl

public actual class ListenerCertificateAuthenticator
internal constructor(override val actual: com.couchbase.lite.ListenerCertificateAuthenticator) :
    DelegatedClass<com.couchbase.lite.ListenerCertificateAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(rootCerts: List<ByteArray>) : this(
        com.couchbase.lite.ListenerCertificateAuthenticator(rootCerts.map { X509CertImpl(it) })
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        com.couchbase.lite.ListenerCertificateAuthenticator(delegate.convert())
    )
}
