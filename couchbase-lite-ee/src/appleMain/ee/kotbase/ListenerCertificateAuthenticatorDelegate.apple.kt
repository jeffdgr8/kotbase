package kotbase

import cocoapods.CouchbaseLite.CBLListenerCertificateAuthenticatorBlock
import kotbase.ext.toByteArray
import platform.Security.SecCertificateRef

internal fun ListenerCertificateAuthenticatorDelegate.convert(): CBLListenerCertificateAuthenticatorBlock {
    return { certs ->
        invoke(certs!!.map {
            @Suppress("UNCHECKED_CAST")
            (it as SecCertificateRef).toByteArray()
        })
    }
}
