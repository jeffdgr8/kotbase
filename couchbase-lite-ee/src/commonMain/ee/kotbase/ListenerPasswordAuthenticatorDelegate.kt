package kotbase

/**
 * Functional Interface for an Authenticator that uses an authentication strategy based on a user name and password.
 * Pass implementations of this interface to the [ListenerPasswordAuthenticator] to realize
 * specific authentication strategies.
 *
 * Authenticate a client based on the passed credentials.
 *
 * @param username client supplied username
 * @param password client supplied password
 * @return true when the client is authorized.
 */
public typealias ListenerPasswordAuthenticatorDelegate =
            (username: String, password: CharArray) -> Boolean
