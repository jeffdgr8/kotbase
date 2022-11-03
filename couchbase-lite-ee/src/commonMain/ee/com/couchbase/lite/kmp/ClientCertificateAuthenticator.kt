package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * An authenticator for client certificate authentication which happens during
 * the TLS handshake when connecting to a server.
 *
 * The client certificate authenticator is currently used only for authenticating to
 * a URLEndpointListener.  The URLEndpointListener must have TLS enabled and
 * must be configured with a ListenerCertificateAuthenticator to verify client
 * certificates.
 */
public expect class ClientCertificateAuthenticator

/**
 * Creates a ClientCertificateAuthenticator object with the given client identity.
 *
 * @param identity client identity
 */
constructor(identity: TLSIdentity) : Authenticator {

    /**
     * The client identity.
     */
    public val identity: TLSIdentity
}
