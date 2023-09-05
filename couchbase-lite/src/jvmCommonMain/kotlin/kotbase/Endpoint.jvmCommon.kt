package kotbase

import com.couchbase.lite.Endpoint as CBLEndpoint
import com.couchbase.lite.URLEndpoint as CBLURLEndpoint

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface Endpoint {

    public val actual: CBLEndpoint
}
