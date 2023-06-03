package kotbase

import com.couchbase.lite.ListenerPasswordAuthenticator
import kotbase.base.DelegatedClass

public actual class ListenerPasswordAuthenticator
internal constructor(override val actual: com.couchbase.lite.ListenerPasswordAuthenticator) :
    DelegatedClass<ListenerPasswordAuthenticator>(actual),
    ListenerAuthenticator {

    public actual constructor(delegate: ListenerPasswordAuthenticatorDelegate) : this(
        com.couchbase.lite.ListenerPasswordAuthenticator(delegate.convert())
    )
}
