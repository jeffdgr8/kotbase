package kotbase

internal expect class ListenerAuthenticatorPlatformState

/**
 * **ENTERPRISE EDITION API**
 *
 * The authenticator used by URLEndpointListener to authenticate clients.
 */
public expect sealed class ListenerAuthenticator {

    internal val platformState: ListenerAuthenticatorPlatformState?
}
