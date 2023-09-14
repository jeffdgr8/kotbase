package kotbase

internal expect class EndpointPlatformState

/**
 * Replication target endpoint
 */
public expect sealed class Endpoint {

    internal val platformState: EndpointPlatformState
}
