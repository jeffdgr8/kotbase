package kotbase

import com.couchbase.lite.Authenticator as CBLAuthenticator

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface Authenticator {

    public val actual: CBLAuthenticator
}
