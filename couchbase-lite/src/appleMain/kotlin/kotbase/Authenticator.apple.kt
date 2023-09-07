package kotbase

import cocoapods.CouchbaseLite.CBLAuthenticator

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface Authenticator {

    public val actual: CBLAuthenticator
}
