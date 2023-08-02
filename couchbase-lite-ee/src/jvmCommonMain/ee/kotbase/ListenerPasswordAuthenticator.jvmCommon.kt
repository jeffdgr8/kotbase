package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ListenerPasswordAuthenticator as CBLListenerPasswordAuthenticator

public actual class ListenerPasswordAuthenticator
internal constructor(override val actual: CBLListenerPasswordAuthenticator) :
    DelegatedClass<CBLListenerPasswordAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(delegate: ListenerPasswordAuthenticatorDelegate) : this(
        CBLListenerPasswordAuthenticator(delegate.convert())
    )
}
