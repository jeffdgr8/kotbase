package com.couchbase.lite.kmp

public actual class SessionAuthenticator
internal constructor(override val actual: com.couchbase.lite.SessionAuthenticator) :
    Authenticator(actual) {

    public actual constructor(sessionID: String, cookieName: String?) : this(
        com.couchbase.lite.SessionAuthenticator(sessionID, cookieName)
    )

    public actual val sessionID: String
        get() = actual.sessionID

    public actual val cookieName: String?
        get() = actual.cookieName
}
