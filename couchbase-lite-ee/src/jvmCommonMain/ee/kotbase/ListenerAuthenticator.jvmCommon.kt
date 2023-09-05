package kotbase

import com.couchbase.lite.ListenerAuthenticator as CBLListenerAuthenticator

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface ListenerAuthenticator {

    public val actual: CBLListenerAuthenticator
}
