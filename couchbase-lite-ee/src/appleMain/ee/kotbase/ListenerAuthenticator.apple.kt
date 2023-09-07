package kotbase

import cocoapods.CouchbaseLite.CBLListenerAuthenticatorProtocol

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface ListenerAuthenticator {

    public val actual: CBLListenerAuthenticatorProtocol
}
