@file:JvmName("ListenerCertificateAuthenticatorDelegateJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.ListenerCertificateAuthenticatorDelegate as CBLListenerCertificateAuthenticatorDelegate

internal fun ListenerCertificateAuthenticatorDelegate.convert(): CBLListenerCertificateAuthenticatorDelegate {
    return CBLListenerCertificateAuthenticatorDelegate { certs ->
        invoke(certs.map { it.encoded })
    }
}
