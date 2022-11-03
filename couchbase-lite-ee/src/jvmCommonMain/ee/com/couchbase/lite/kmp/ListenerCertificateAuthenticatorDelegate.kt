package com.couchbase.lite.kmp

internal fun ListenerCertificateAuthenticatorDelegate.convert(): com.couchbase.lite.ListenerCertificateAuthenticatorDelegate {
    return com.couchbase.lite.ListenerCertificateAuthenticatorDelegate { certs ->
        invoke(certs.map { it.encoded })
    }
}
