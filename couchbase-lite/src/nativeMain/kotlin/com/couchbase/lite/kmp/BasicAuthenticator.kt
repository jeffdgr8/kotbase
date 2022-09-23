package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toFLString
import libcblite.CBLAuth_CreatePassword

public actual class BasicAuthenticator
actual constructor(
    @Suppress("CanBeParameter")
    public actual val username: String,
    password: CharArray
) : Authenticator(
    CBLAuth_CreatePassword(username.toFLString(), password.concatToString().toFLString())!!
) {

    public actual val passwordChars: CharArray = password
}
