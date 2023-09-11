package kotbase

import com.couchbase.lite.Endpoint as CBLEndpoint

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface Endpoint {

    public val actual: CBLEndpoint
}
