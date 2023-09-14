package kotbase

import com.couchbase.lite.ListenerPasswordAuthenticator as CBLListenerPasswordAuthenticator

public actual class ListenerPasswordAuthenticator
internal constructor(actual: CBLListenerPasswordAuthenticator) : ListenerAuthenticator(actual) {

    public actual constructor(delegate: ListenerPasswordAuthenticatorDelegate) : this(
        CBLListenerPasswordAuthenticator(delegate.convert())
    )
}

internal val ListenerPasswordAuthenticator.actual: CBLListenerPasswordAuthenticator
    get() = platformState!!.actual as CBLListenerPasswordAuthenticator
