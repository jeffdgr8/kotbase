package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual abstract class Authenticator
internal constructor(actual: com.couchbase.lite.Authenticator) :
    DelegatedClass<com.couchbase.lite.Authenticator>(actual)

internal fun com.couchbase.lite.Authenticator.toAuthenticator(): Authenticator {
    return when (this) {
        is com.couchbase.lite.BasicAuthenticator -> BasicAuthenticator(this)
        is com.couchbase.lite.SessionAuthenticator -> SessionAuthenticator(this)
        else -> error("Unknown Authenticator type ${this::class}")
    }
}
