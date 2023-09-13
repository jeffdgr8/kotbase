package kotbase

import com.couchbase.lite.Authenticator as CBLAuthenticator

internal actual class AuthenticatorPlatformState(
    internal val actual: CBLAuthenticator
)

public actual sealed class Authenticator(actual: CBLAuthenticator) {

    internal actual val platformState = AuthenticatorPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual == (other as? Authenticator)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val Authenticator.actual: CBLAuthenticator
    get() = platformState.actual
