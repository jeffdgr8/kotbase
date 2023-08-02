package kotbase

import com.couchbase.lite.ListenerAuthenticator as CBLListenerAuthenticator

public actual interface ListenerAuthenticator {

    public val actual: CBLListenerAuthenticator
}
