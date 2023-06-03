@file:JvmName("ListenerCertificateAuthenticatorDelegateJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

internal fun ListenerCertificateAuthenticatorDelegate.convert(): com.couchbase.lite.ListenerCertificateAuthenticatorDelegate {
    return com.couchbase.lite.ListenerCertificateAuthenticatorDelegate { certs ->
        invoke(certs.map { it.encoded })
    }
}
