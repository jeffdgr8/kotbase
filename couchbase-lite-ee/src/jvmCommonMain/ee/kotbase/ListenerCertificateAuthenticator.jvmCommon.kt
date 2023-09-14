package kotbase

import kotbase.ext.toCertificates
import com.couchbase.lite.ListenerCertificateAuthenticator as CBLListenerCertificateAuthenticator

public actual class ListenerCertificateAuthenticator
internal constructor(actual: CBLListenerCertificateAuthenticator) : ListenerAuthenticator(actual) {

    public actual constructor(rootCerts: List<ByteArray>) : this(
        CBLListenerCertificateAuthenticator(rootCerts.toCertificates())
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        CBLListenerCertificateAuthenticator(delegate.convert())
    )
}

internal val ListenerCertificateAuthenticator.actual: CBLListenerCertificateAuthenticator
    get() = platformState!!.actual as CBLListenerCertificateAuthenticator
