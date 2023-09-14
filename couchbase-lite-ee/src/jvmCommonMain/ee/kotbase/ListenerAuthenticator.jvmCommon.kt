package kotbase

import com.couchbase.lite.ListenerAuthenticator as CBLListenerAuthenticator

internal actual class ListenerAuthenticatorPlatformState(
    internal val actual: CBLListenerAuthenticator
)

public actual sealed class ListenerAuthenticator(actual: CBLListenerAuthenticator) {

    internal actual val platformState: ListenerAuthenticatorPlatformState? = ListenerAuthenticatorPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual == (other as? ListenerAuthenticator)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val ListenerAuthenticator.actual: CBLListenerAuthenticator
    get() = platformState!!.actual
