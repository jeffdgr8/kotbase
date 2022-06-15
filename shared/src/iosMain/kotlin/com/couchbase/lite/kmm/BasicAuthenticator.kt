package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLBasicAuthenticator

public actual class BasicAuthenticator
internal constructor(override val actual: CBLBasicAuthenticator) :
    Authenticator(actual) {

    public actual constructor(username: String, password: CharArray) : this(
        CBLBasicAuthenticator(username, password.concatToString())
    )

    public actual val username: String
        get() = actual.username

    public actual val passwordChars: CharArray
        get() = actual.password.toCharArray()
}
