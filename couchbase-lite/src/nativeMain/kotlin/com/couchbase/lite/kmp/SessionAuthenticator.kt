package com.couchbase.lite.kmp

import cnames.structs.CBLAuthenticator
import com.couchbase.lite.kmp.internal.fleece.toFLString
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_CreateSession
import libcblite.CBLAuth_Free
import kotlin.native.internal.createCleaner

public actual class SessionAuthenticator
@Suppress("CanBeParameter")
actual constructor(
    public actual val sessionID: String,
    cookieName: String?
) : Authenticator {

    override val actual: CPointer<CBLAuthenticator> =
        CBLAuth_CreateSession(
            sessionID.toFLString(),
            (cookieName ?: DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME).toFLString()
        )!!

    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLAuth_Free(it)
    }

    public actual val cookieName: String = cookieName ?: DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME

    private companion object {
        private const val DEFAULT_SYNC_GATEWAY_SESSION_ID_NAME = "SyncGatewaySession"
    }
}
