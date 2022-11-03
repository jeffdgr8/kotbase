package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLListenerCertificateAuthenticatorBlock
import com.udobny.kmp.ext.toByteArray
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateRef

internal fun ListenerCertificateAuthenticatorDelegate.convert(): CBLListenerCertificateAuthenticatorBlock {
    return { certs ->
        invoke(certs!!.map {
            @Suppress("UNCHECKED_CAST")
            SecCertificateCopyData(it as SecCertificateRef)!!.toByteArray()
        })
    }
}
