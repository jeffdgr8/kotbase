package kotbase

import com.couchbase.lite.Authenticator as CBLAuthenticator

public actual interface Authenticator {

    public val actual: CBLAuthenticator
}
