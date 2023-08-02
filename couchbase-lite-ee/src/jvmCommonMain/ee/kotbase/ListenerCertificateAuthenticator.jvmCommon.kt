package kotbase

import kotbase.base.DelegatedClass
import java.io.ByteArrayInputStream
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
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

private fun List<ByteArray>.toCertificates(): List<Certificate> {
    val certFactory = CertificateFactory.getInstance("X.509")
    return map { certFactory.generateCertificate(ByteArrayInputStream(it)) }
}
