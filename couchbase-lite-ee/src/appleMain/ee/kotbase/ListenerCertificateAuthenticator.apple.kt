package kotbase

import cocoapods.CouchbaseLite.CBLListenerCertificateAuthenticator
import kotbase.ext.toSecCertificate

public actual class ListenerCertificateAuthenticator
internal constructor(actual: CBLListenerCertificateAuthenticator) : ListenerAuthenticator(actual) {

    public actual constructor(rootCerts: List<ByteArray>) : this(
        CBLListenerCertificateAuthenticator(
            rootCerts.map { it.toSecCertificate() }
        )
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        CBLListenerCertificateAuthenticator(delegate.convert())
    )
}

internal val ListenerCertificateAuthenticator.actual: CBLListenerCertificateAuthenticator
    get() = platformState!!.actual as CBLListenerCertificateAuthenticator
