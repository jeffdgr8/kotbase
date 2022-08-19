package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLAuthenticator
import cocoapods.CouchbaseLite.CBLBasicAuthenticator
import cocoapods.CouchbaseLite.CBLSessionAuthenticator
import com.udobny.kmp.DelegatedClass

public actual abstract class Authenticator
internal constructor(actual: CBLAuthenticator) :
    DelegatedClass<CBLAuthenticator>(actual)

internal fun CBLAuthenticator.toAuthenticator(): Authenticator {
    return when (this) {
        is CBLBasicAuthenticator -> BasicAuthenticator(this)
        is CBLSessionAuthenticator -> SessionAuthenticator(this)
        else -> error("Unknown Authenticator type ${this::class}")
    }
}
