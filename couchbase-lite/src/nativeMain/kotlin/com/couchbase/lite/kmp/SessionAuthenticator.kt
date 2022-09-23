package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toFLString
import libcblite.CBLAuth_CreateSession

public actual class SessionAuthenticator
@Suppress("CanBeParameter")
actual constructor(
    public actual val sessionID: String,
    public actual val cookieName: String?
) : Authenticator(CBLAuth_CreateSession(sessionID.toFLString(), cookieName.toFLString())!!)
