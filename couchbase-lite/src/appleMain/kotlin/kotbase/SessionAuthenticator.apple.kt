package kotbase

import cocoapods.CouchbaseLite.CBLSessionAuthenticator

public actual class SessionAuthenticator
internal constructor(
    internal val actual: CBLSessionAuthenticator
) : Authenticator(actual) {

    public actual constructor(sessionID: String, cookieName: String?) : this(
        CBLSessionAuthenticator(sessionID, cookieName)
    )

    public actual val sessionID: String
        get() = actual.sessionID

    public actual val cookieName: String
        get() = actual.cookieName
}
