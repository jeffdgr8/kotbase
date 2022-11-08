package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLListenerCertificateAuthenticator
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toSecCertificate

public actual class ListenerCertificateAuthenticator
internal constructor(override val actual: CBLListenerCertificateAuthenticator) :
    DelegatedClass<CBLListenerCertificateAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(rootCerts: List<ByteArray>) : this(
        CBLListenerCertificateAuthenticator(
            rootCerts.map { it.toSecCertificate() }
        )
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        CBLListenerCertificateAuthenticator(delegate.convert())
    )
}
