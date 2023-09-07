package kotbase

import cnames.structs.CBLEndpoint
import kotlinx.cinterop.CPointer

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual interface Endpoint {

    public val actual: CPointer<CBLEndpoint>
}
