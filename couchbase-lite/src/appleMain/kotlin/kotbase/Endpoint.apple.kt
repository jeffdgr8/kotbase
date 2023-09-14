package kotbase

import cocoapods.CouchbaseLite.CBLEndpointProtocol

internal actual class EndpointPlatformState(
    internal val actual: CBLEndpointProtocol
)

public actual sealed class Endpoint(actual: CBLEndpointProtocol) {

    internal actual val platformState = EndpointPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? Endpoint)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal val Endpoint.actual: CBLEndpointProtocol
    get() = platformState.actual
