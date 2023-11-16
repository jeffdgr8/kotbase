/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

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
    @Deprecated(
        "Use URLEndpointListenerConfiguration(Collections)",
        ReplaceWith("URLEndpointListenerConfiguration(setOf(database.getDefaultCollection()), networkInterface, port, disableTls, identity, authenticator, readOnly, enableDeltaSync)")
    )
    public constructor(
        database: Database,
        networkInterface: String? = null,
        port: Int = Defaults.Listener.PORT,
        disableTls: Boolean = Defaults.Listener.DISABLE_TLS,
        identity: TLSIdentity? = null,
        authenticator: ListenerAuthenticator? = null,
        readOnly: Boolean = Defaults.Listener.READ_ONLY,
        enableDeltaSync: Boolean = Defaults.Listener.ENABLE_DELTA_SYNC
    )

    /**
     * Create a URLEndpointListenerConfiguration with the passed properties. The set of passed Collections
     * must contain at least one collection and all of the collections it contains must belong
     * to the same scope and the same database, otherwise an InvalidArgumentException will be thrown.
     * If one of the specified collections is deleted during replication, connected clients will be closed
     * with an error.
     *
     * @param collections      the collections to which the listener is attached
     * @param networkInterface the name of the interface on which to receive connections
     * @param port             the ip port (0 - 65535) on which to configure the listener. Default is 0: first available
     * @param disableTls       set true to turn of TLS.  Default is false
     * @param identity         the identity this listener will use to authenticate itself
     * @param authenticator    the predicate used to authenticate clients
     * @param readOnly         set true to prevent modification of the local connections
     * @param enableDeltaSync  set true to turn on fast synching.
     */
    public constructor(
        collections: Set<Collection>,
        networkInterface: String? = null,
        port: Int = Defaults.Listener.PORT,
        disableTls: Boolean = Defaults.Listener.DISABLE_TLS,
        identity: TLSIdentity? = null,
        authenticator: ListenerAuthenticator? = null,
        readOnly: Boolean = Defaults.Listener.READ_ONLY,
        enableDeltaSync: Boolean = Defaults.Listener.ENABLE_DELTA_SYNC
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
    @Deprecated("Use collections")
    public val database: Database

    /**
     * Get the configured collections.
     */
    public val collections: Set<Collection>

    /**
     * The name of the configured network interface on which to configure the listener (e.g. "en0")
     */
    public var networkInterface: String?

    /**
     * The port number on which to configure the listener (between 0 and 65535, inclusive).
     *
     * A port number of 0 (the default) tells the OS to choose some available port.
     */
    public var port: Int?

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

    public companion object
}

public val URLEndpointListenerConfiguration.Companion.MIN_PORT: Int
    get() = 0

public val URLEndpointListenerConfiguration.Companion.MAX_PORT: Int
    get() = 65535
