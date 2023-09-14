package kotbase

import cocoapods.CouchbaseLite.CBLListenerAuthenticatorProtocol

internal actual class ListenerAuthenticatorPlatformState(
    internal val actual: CBLListenerAuthenticatorProtocol
)

public actual sealed class ListenerAuthenticator(actual: CBLListenerAuthenticatorProtocol) {

    internal actual val platformState = ListenerAuthenticatorPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? ListenerAuthenticator)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal val ListenerAuthenticator.actual: CBLListenerAuthenticatorProtocol
    get() = platformState.actual
