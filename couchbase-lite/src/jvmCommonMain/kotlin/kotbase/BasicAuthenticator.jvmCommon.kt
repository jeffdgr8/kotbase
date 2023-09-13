package kotbase

import com.couchbase.lite.BasicAuthenticator as CBLBasicAuthenticator

public actual class BasicAuthenticator
internal constructor(
    actual: CBLBasicAuthenticator
) : Authenticator(actual) {

    public actual constructor(username: String, password: CharArray) : this(CBLBasicAuthenticator(username, password))

    public actual val username: String
        get() = actual.username

    public actual val passwordChars: CharArray
        get() = actual.passwordChars
}

internal val BasicAuthenticator.actual: CBLBasicAuthenticator
    get() = platformState.actual as CBLBasicAuthenticator
