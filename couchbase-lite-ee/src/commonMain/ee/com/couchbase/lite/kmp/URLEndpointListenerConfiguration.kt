package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * Configuration information for a URL endpoint listener.
 * There are two varieties: Http and Tls.
 */
public expect class URLEndpointListenerConfiguration {

    /**
     * Create a listener configuration, for the specified database, with default values.
     *
     * @param database the database to which the listener is attached
     */
    public constructor(
        database: Database,
        networkInterface: String? = null,
        port: Int = 0,
        disableTls: Boolean = false,
        identity: TLSIdentity? = null,
        authenticator: ListenerAuthenticator? = null,
        readOnly: Boolean = false,
        enableDeltaSync: Boolean = false
    )

    /**
     * Clone the passed listener configuration.
     *
     * @param config the configuration to duplicate
     */
    public constructor(config: URLEndpointListenerConfiguration)

    /**
     * The configured database.
     */
    public val database: Database

    /**
     * The name of the configured network interface on which to configure the listener (e.g. "en0")
     */
    public var networkInterface: String?

    /**
     * The port number on which to configure the listener (between 0 and 65535, inclusive).
     *
     * A port number of 0 (the default) tells the OS to choose some available port.
     */
    public var port: Int

    /**
     * If this configuration will disable TLS in its associated listener.
     *
     * TLS is enabled by default. disabling it is not recommended for production.
     */
    public var isTlsDisabled: Boolean

    /**
     * The TLS identity with the certificates
     * and keys for the associated listener.
     */
    public var tlsIdentity: TLSIdentity?

    /**
     * When TLS is enabled, a null authenticator (the default) will allow clients
     * whose certificate chains can be verified by one of the OS-bundled root certificates. There are two
     * types of TLS authenticators. See [ListenerCertificateAuthenticator]
     *
     * When TLS is disabled, a null authenticator (the default) will allow all clients. A non-null
     * authenticator will be passed the client's credentials and is completely responsible for
     * authenticating them. See [ListenerPasswordAuthenticator]
     */
    public var authenticator: ListenerAuthenticator?

    /**
     * Is connection read-only.
     */
    public var isReadOnly: Boolean

    /**
     * Is delta sync enabled.
     */
    public var isDeltaSyncEnabled: Boolean
}

@Suppress("unused")
public val URLEndpointListenerConfiguration.MIN_PORT: Int
    get() = 0

@Suppress("unused")
public val URLEndpointListenerConfiguration.MAX_PORT: Int
    get() = 65535
