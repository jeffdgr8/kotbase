package kotbase

import com.couchbase.lite.ListenerCertificateAuthenticatorDelegate as CBLListenerCertificateAuthenticatorDelegate

internal fun ListenerCertificateAuthenticatorDelegate.convert(): CBLListenerCertificateAuthenticatorDelegate {
    return CBLListenerCertificateAuthenticatorDelegate { certs ->
        invoke(certs.map { it.encoded })
    }
}
