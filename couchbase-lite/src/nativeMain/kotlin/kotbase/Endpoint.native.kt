package kotbase

import cnames.structs.CBLEndpoint
import kotlinx.cinterop.CPointer

internal actual class EndpointPlatformState(
    internal val actual: CPointer<CBLEndpoint>
)

public actual sealed class Endpoint(actual: CPointer<CBLEndpoint>) {

    internal actual val platformState = EndpointPlatformState(actual)
}

internal val Endpoint.actual: CPointer<CBLEndpoint>
    get() = platformState.actual
