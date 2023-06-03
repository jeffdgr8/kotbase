package kotbase

import cocoapods.CouchbaseLite.CBLListenerCertificateAuthenticator
import kotbase.base.DelegatedClass
import kotbase.ext.toSecCertificate

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
