package com.couchbase.lite.kmp

import cnames.structs.CBLAuthenticator
import com.couchbase.lite.kmp.internal.fleece.toFLString
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_CreatePassword

public actual class BasicAuthenticator
actual constructor(
    @Suppress("CanBeParameter")
    public actual val username: String,
    password: CharArray
) : Authenticator {

    override val actual: CPointer<CBLAuthenticator> =
        CBLAuth_CreatePassword(username.toFLString(), password.concatToString().toFLString())!!

    public actual val passwordChars: CharArray = password
}
