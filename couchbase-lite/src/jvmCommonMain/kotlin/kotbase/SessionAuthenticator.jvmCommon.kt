package kotbase

import com.couchbase.lite.SessionAuthenticator as CBLSessionAuthenticator

public actual class SessionAuthenticator
internal constructor(
    override val actual: CBLSessionAuthenticator
) : Authenticator {

    public actual constructor(sessionID: String, cookieName: String?) : this(
        CBLSessionAuthenticator(sessionID, cookieName)
    )

    public actual val sessionID: String
        get() = actual.sessionID

    public actual val cookieName: String
        get() = actual.cookieName!!
}