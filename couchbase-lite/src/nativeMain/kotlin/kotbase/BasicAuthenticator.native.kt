package kotbase

import cnames.structs.CBLAuthenticator
import kotbase.internal.fleece.toFLString
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_CreatePassword

public actual class BasicAuthenticator
private constructor(
    public actual val username: String,
    public actual val passwordChars: CharArray,
    actual: CPointer<CBLAuthenticator>
) : Authenticator(actual) {

    public actual constructor(username: String, password: CharArray) : this(
        username,
        password,
        CBLAuth_CreatePassword(username.toFLString(), password.concatToString().toFLString())!!
    )
}
