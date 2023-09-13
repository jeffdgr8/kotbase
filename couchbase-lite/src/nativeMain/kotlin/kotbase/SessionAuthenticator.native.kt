package kotbase

import cnames.structs.CBLAuthenticator
import kotbase.internal.fleece.toFLString
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_CreateSession

public actual class SessionAuthenticator
private constructor(
    public actual val sessionID: String,
    public actual val cookieName: String,
    actual: CPointer<CBLAuthenticator>
) : Authenticator(actual) {

    public actual constructor(sessionID: String, cookieName: String?) : this(
        sessionID,
        cookieName ?: DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME,
        CBLAuth_CreateSession(
            sessionID.toFLString(),
            (cookieName ?: DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME).toFLString()
        )!!
    )

    private companion object {
        private const val DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME = "SyncGatewaySession"
    }
}
