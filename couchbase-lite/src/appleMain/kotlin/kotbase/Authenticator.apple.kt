package kotbase

import cocoapods.CouchbaseLite.CBLAuthenticator

internal actual class AuthenticatorPlatformState(
    internal val actual: CBLAuthenticator
)

public actual sealed class Authenticator(actual: CBLAuthenticator) {

    internal actual val platformState = AuthenticatorPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? Authenticator)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal val Authenticator.actual: CBLAuthenticator
    get() = platformState.actual
