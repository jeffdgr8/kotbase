/*
 * Copyright 2026 Jeff Lockhart
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
 * Configuration for a MultipeerReplicator.
 *
 * This configuration is used to set up an immutable [MultipeerReplicator]
 * with necessary parameters.
 *
 * @constructor Initializes the configuration with a peer group identifier,
 * identity, authenticator and collections.
 *
 * @param peerGroupID The group identifier for discovering and connecting peers.
 * @param identity The peer's TLS identity. The certificate must be valid for both client and server authentication.
 * @param authenticator Peer authenticator for verifying the connecting peer's certificate.
 * @param collections A collection of [MultipeerCollectionConfiguration] instances
 *     representing the collections to replicate and their configurations.
 * @param transports Transport options for peer discovery and replication. The default is Wi-Fi only.
 */
public class MultipeerReplicatorConfiguration(
    peerGroupID: String,

    /**
     * Peer identity.
     *
     * Note: The identity's certificate must be both server and client certificate.
     */
    public var identity: TLSIdentity,

    /**
     * Peer authenticator for verifying the connecting peer's certificate.
     */
    public var authenticator: MultipeerCertificateAuthenticator,

    collections: kotlin.collections.Collection<MultipeerCollectionConfiguration>,
    transports: Set<MultipeerTransport> = setOf(MultipeerTransport.WIFI)
) {

    /**
     * Clone an existing configuration.
     *
     * @param config A multipeer replicator configuration to copy.
     */
    public constructor(config: MultipeerReplicatorConfiguration) : this(
        config.peerGroupID,
        config.identity,
        config.authenticator,
        config.collections.map { MultipeerCollectionConfiguration(it) },
        config.transports
    )

    /**
     * Identifier for discovering and connecting peers.
     */
    public var peerGroupID: String = checkPeerGroupID(peerGroupID)
        set(value) {
            field = checkPeerGroupID(value)
        }

    /**
     * A set of collections with their configurations, to be replicated.
     */
    public var collections: Set<MultipeerCollectionConfiguration> = checkCollections(collections)
        set(value) {
            field = checkCollections(value)
        }

    /**
     * Transport options for peer discovery and replication. The default is Wi-Fi only.
     * When multiple transports are specified, the replicator automatically selects and
     * switches between available transports, preferring Wi-Fi over Bluetooth.
     *
     * Setting an empty set will reset the transports to the default, Wi-Fi only.
     */
    public var transports: Set<MultipeerTransport> = defaultTransports(transports)
        set(value) {
            field = defaultTransports(value)
        }

    /**
     * Sets the peer group ID.
     *
     * @param peerGroupID The peer group ID.
     * @return this.
     */
    public fun setPeerGroupID(peerGroupID: String): MultipeerReplicatorConfiguration {
        this.peerGroupID = peerGroupID
        return this
    }

    /**
     * Set the peer identity.
     *
     * Note: The identity's certificate must be both server and client certificate.
     *
     * @param identity The peer identity.
     * @return this.
     */
    public fun setIdentity(identity: TLSIdentity): MultipeerReplicatorConfiguration {
        this.identity = identity
        return this
    }

    /**
     * Set the peer authenticator.
     *
     * @param authenticator The peer authenticator.
     * @return this.
     */
    public fun setAuthenticator(authenticator: MultipeerCertificateAuthenticator): MultipeerReplicatorConfiguration {
        this.authenticator = authenticator
        return this
    }

    /**
     * Sets the collections to replicate, each with its own configuration, such as filters
     * and a custom conflict resolver.
     *
     * If custom configuration is not needed, use [MultipeerCollectionConfiguration.fromCollections]
     * to create configurations directly from a collection of [Collection] instances.
     *
     * @param collections A collection of [MultipeerCollectionConfiguration]
     *     instances representing the collections and their configurations.
     * @return this.
     */
    public fun setCollections(
        collections: kotlin.collections.Collection<MultipeerCollectionConfiguration>
    ): MultipeerReplicatorConfiguration {
        this.collections = collections.toSet()
        return this
    }

    /**
     * Set the transport options for peer discovery and replication.
     *
     * @param transports The transport options. An empty set will reset
     *     the transports to the default, Wi-Fi only.
     * @return this.
     */
    public fun setTransports(transports: Set<MultipeerTransport>): MultipeerReplicatorConfiguration {
        this.transports = transports
        return this
    }

    private companion object {

        private fun checkPeerGroupID(peerGroupID: String): String {
            require(peerGroupID.isNotEmpty()) { "peerGroupID must not be empty" }
            return peerGroupID
        }

        private fun checkCollections(
            collections: kotlin.collections.Collection<MultipeerCollectionConfiguration>
        ): Set<MultipeerCollectionConfiguration> {
            require(collections.isNotEmpty()) { "collections must not be empty" }
            return collections.toSet()
        }

        private fun defaultTransports(transports: Set<MultipeerTransport>): Set<MultipeerTransport> =
            transports.ifEmpty { setOf(MultipeerTransport.WIFI) }
    }
}
