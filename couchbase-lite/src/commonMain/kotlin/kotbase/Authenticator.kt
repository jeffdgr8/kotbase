package kotbase

internal expect class AuthenticatorPlatformState

/**
 * Authenticator objects provide server authentication credentials to the replicator.
 * Authenticator is a base sealed class; you must instantiate one of its implementations.
 */
public expect sealed class Authenticator {

    internal val platformState: AuthenticatorPlatformState
}
