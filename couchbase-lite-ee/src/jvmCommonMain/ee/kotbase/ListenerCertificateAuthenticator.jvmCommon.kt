package kotbase

import kotbase.base.DelegatedClass
import kotbase.ext.toCertificates
import com.couchbase.lite.ListenerCertificateAuthenticator as CBLListenerCertificateAuthenticator

public actual class ListenerCertificateAuthenticator
internal constructor(override val actual: CBLListenerCertificateAuthenticator) :
    DelegatedClass<CBLListenerCertificateAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(rootCerts: List<ByteArray>) : this(
        CBLListenerCertificateAuthenticator(rootCerts.toCertificates())
    )

    public actual constructor(delegate: ListenerCertificateAuthenticatorDelegate) : this(
        CBLListenerCertificateAuthenticator(delegate.convert())
    )
}
