package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import java.io.ByteArrayInputStream
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

public actual class ListenerCertificateAuthenticator
internal constructor(override val actual: com.couchbase.lite.ListenerCertificateAuthenticator) :
    DelegatedClass<com.couchbase.lite.ListenerCertificateAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(rootCerts: List<ByteArray>) : this(
        com.couchbase.lite.ListenerCertificateAuthenticator(rootCerts.toCertificates())
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        com.couchbase.lite.ListenerCertificateAuthenticator(delegate.convert())
    )
}

private fun List<ByteArray>.toCertificates(): List<Certificate> {
    val certFactory = CertificateFactory.getInstance("X.509")
    return map { certFactory.generateCertificate(ByteArrayInputStream(it)) }
}