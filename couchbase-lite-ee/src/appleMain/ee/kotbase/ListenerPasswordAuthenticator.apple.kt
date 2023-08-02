package kotbase

import cocoapods.CouchbaseLite.CBLListenerPasswordAuthenticator
import kotbase.base.DelegatedClass

public actual class ListenerPasswordAuthenticator
internal constructor(override val actual: CBLListenerPasswordAuthenticator) :
    DelegatedClass<CBLListenerPasswordAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(delegate: ListenerPasswordAuthenticatorDelegate) : this(
        CBLListenerPasswordAuthenticator(delegate.convert())
    )
}
