package kotbase

import cocoapods.CouchbaseLite.CBLEndpointProtocol

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface Endpoint {

    public val actual: CBLEndpointProtocol
}
