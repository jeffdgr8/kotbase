package kotbase

internal expect class AuthenticatorPlatformState

/**
 * Authenticator is an opaque interface.
 */
public expect sealed class Authenticator {

    internal val platformState: AuthenticatorPlatformState
}
