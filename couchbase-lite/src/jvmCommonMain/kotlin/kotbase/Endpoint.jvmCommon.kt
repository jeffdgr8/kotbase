package kotbase

import com.couchbase.lite.Endpoint as CBLEndpoint

internal actual class EndpointPlatformState(
    internal val actual: CBLEndpoint
)

public actual sealed class Endpoint(actual: CBLEndpoint) {

    internal actual val platformState = EndpointPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual == (other as? Endpoint)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val Endpoint.actual: CBLEndpoint
    get() = platformState.actual
