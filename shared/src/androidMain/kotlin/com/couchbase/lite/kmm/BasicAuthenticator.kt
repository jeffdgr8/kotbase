package com.couchbase.lite.kmm

public actual class BasicAuthenticator
internal constructor(override val actual: com.couchbase.lite.BasicAuthenticator) :
    Authenticator(actual) {

    public actual constructor(username: String, password: CharArray) :
            this(com.couchbase.lite.BasicAuthenticator(username, password))

    public actual val username: String
        get() = actual.username

    public actual val passwordChars: CharArray
        get() = actual.passwordChars
}
