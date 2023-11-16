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

import com.couchbase.lite.generation

/**
 * Configuration for a Replicator
 */
public expect class ReplicatorConfiguration {

    /**
     * Create a Replicator Configuration
     *
     * @param database the database to be synchronized
     * @param target   the endpoint with which to synchronize it
     */
    @Deprecated(
        "Use ReplicatorConfiguration(Endpoint)",
        ReplaceWith("ReplicatorConfiguration(target)")
    )
    public constructor(database: Database, target: Endpoint)

    /**
     * Create a Replicator Configuration
     *
     * @param target the target endpoint
     */
    public constructor(target: Endpoint)

    /**
     * Create a Replicator Configuration
     *
     * @param config the config to copy
     */
    public constructor(config: ReplicatorConfiguration)

    /**
     * Add a collection used for the replication with an optional collection configuration.
     * If the collection has been added before, the previously added collection
     * and its configuration if specified will be replaced.
     *
     * @param collection the collection
     * @param config     its configuration
     * @return this
     */
    public fun addCollection(collection: Collection, config: CollectionConfiguration?): ReplicatorConfiguration

    /**
     * Add multiple collections used for the replication with an optional shared collection configuration.
     * If any of the collections have been added before, the previously added collections and their
     * configuration if specified will be replaced. Adding an empty collection array is a no-op.
     *
     * @param collections a collection of Collections
     * @param config      the configuration to be applied to all of the collections
     * @return this
     */
    public fun addCollections(
        collections: kotlin.collections.Collection<Collection>,
        config: CollectionConfiguration?
    ): ReplicatorConfiguration

    /**
     * Remove a collection from the replication.
     *
     * @param collection the collection to be removed
     * @return this
     */
    public fun removeCollection(collection: Collection): ReplicatorConfiguration

    /**
     * Sets the replicator type indicating the direction of the replicator.
     * The default is ReplicatorType.PUSH_AND_PULL: bi-directional replication.
     *
     * @param type The replicator type.
     * @return this.
     */
    public fun setType(type: ReplicatorType): ReplicatorConfiguration

    /**
     * Sets whether the replicator stays active indefinitely to replicate changed documents.
     * The default is false: the replicator will stop after it finishes replicating changed documents.
     *
     * @param continuous The continuous flag.
     * @return this.
     */
    public fun setContinuous(continuous: Boolean): ReplicatorConfiguration

    /**
     * Enable/disable auto-purge.
     * The default is auto-purge enabled.
     *
     * Note: A document that is blocked by a document Id filter will not be auto-purged
     * regardless of the setting of the auto purge property
     */
    public fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration

    /**
     * Sets the extra HTTP headers to send in all requests to the remote target.
     * The default is no extra headers.
     *
     * @param headers The HTTP Headers.
     * @return this.
     */
    public fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration

    /**
     * The option to remove a restriction that does not allow a replicator to accept cookies
     * from a remote host unless the cookie domain exactly matches the domain of the sender.
     * For instance, when the option is set to false (the default), and the remote host, “bar.foo.com”,
     * sends a cookie for the domain “.foo.com”, the replicator will reject it. If the option
     * is set true, however, the replicator will accept it. This is, in general, dangerous:
     * a host might, for instance, set a cookie for the domain ".com". It is safe only when
     * the replicator is connecting only to known hosts.
     * The default value of this option is false: parent-domain cookies are not accepted
     */
    public fun setAcceptParentDomainCookies(acceptParentCookies: Boolean): ReplicatorConfiguration

    /**
     * Sets the authenticator to authenticate with a remote target server.
     * Currently, there are two types of the authenticators,
     * BasicAuthenticator and SessionAuthenticator, supported.
     * The default is no authenticator.
     *
     * @param authenticator The authenticator.
     * @return this.
     */
    public fun setAuthenticator(authenticator: Authenticator): ReplicatorConfiguration

    /**
     * Sets the certificate used to authenticate the target server.
     * A server will be authenticated if it presents a chain of certificates (possibly of length 1)
     * in which any one of the certificates matches the one passed here.
     * The default is no pinned certificate.
     *
     * @param pinnedCert the SSL certificate.
     * @return this.
     */
    public fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration

    /**
     * Set the max number of retry attempts made after a connection failure.
     * Set to 1 for no retries and to 0 to restore default behavior.
     * The default is 10 total connection attempts (the initial attempt and up to 9 retries) for
     * a one-shot replicator and a very, very large number of retries, for a continuous replicator.
     *
     * @param maxAttempts max retry attempts
     */
    public fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration

    /**
     * Set the max time between retry attempts, in seconds.
     * Time between retries is initially small but backs off exponentially up to this limit.
     * Once the limit is reached the interval between subsequent attempts will be
     * the value set here, until max-attempts attempts have been made.
     * The minimum value legal value is 1 second.
     * The default is 5 minutes (300 seconds). Setting the parameter to 0 will restore the default.
     *
     * @param maxAttemptWaitTime max attempt wait time
     */
    public fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration

    /**
     * Set the heartbeat interval, in seconds.
     * The default is 5 minutes (300 seconds). Setting the parameter to 0 will restore the default.
     *
     * Must be non-negative and less than Integer.MAX_VALUE milliseconds
     */
    public fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration

    /**
     * A collection of document IDs identifying documents to be replicated.
     * If non-empty, only documents with IDs in this collection will be pushed and/or pulled.
     * Default is empty: do not filter documents.
     *
     * @param documentIDs The document IDs.
     * @return this.
     */
    @Deprecated("Use CollectionConfiguration.setDocumentIDs")
    public fun setDocumentIDs(documentIDs: List<String>?): ReplicatorConfiguration

    /**
     * Sets a collection of Sync Gateway channel names from which to pull Documents.
     * If unset, all accessible channels will be pulled.
     * Default is empty: pull from all accessible channels.
     *
     * Note: Channel specifications apply only to replications
     * pulling from a SyncGateway and only the channels visible
     * to the authenticated user. Channel specs are ignored:
     *
     *  * during a push replication.
     *  * during peer-to-peer or database-to-database replication
     *  * when the specified channel is not accessible to the user
     *
     * @param channels The Sync Gateway channel names.
     * @return this.
     */
    @Deprecated("Use CollectionConfiguration.setChannels")
    public fun setChannels(channels: List<String>?): ReplicatorConfiguration

    /**
     * Sets the conflict resolver.
     * Default is `ReplicatorConfiguration.DEFAULT_CONFLICT_RESOLVER`
     *
     * @param conflictResolver A conflict resolver.
     * @return this.
     */
    @Deprecated("Use CollectionConfiguration.setConflictResolver")
    public fun setConflictResolver(conflictResolver: ConflictResolver?): ReplicatorConfiguration

    /**
     * Sets a filter object for validating whether the documents can be pulled from the
     * remote endpoint. Only documents for which the object returns true are replicated.
     * Default is no filter.
     *
     * @param pullFilter The filter to filter the document to be pulled.
     * @return this.
     */
    @Deprecated("Use CollectionConfiguration.setPullFilter")
    public fun setPullFilter(pullFilter: ReplicationFilter?): ReplicatorConfiguration

    /**
     * Sets a filter object for validating whether the documents can be pushed
     * to the remote endpoint.
     * Default is no filter.
     *
     * @param pushFilter The filter to filter the document to be pushed.
     * @return this.
     */
    @Deprecated("Use CollectionConfiguration.setPushFilter")
    public fun setPushFilter(pushFilter: ReplicationFilter?): ReplicatorConfiguration

    /**
     * The replication target to replicate with.
     */
    public val target: Endpoint

    /**
     * Get the CollectionConfiguration for the passed Collection.
     *
     * @param collection a collection whose configuration is sought.
     * @return the collections configuration
     */
    public fun getCollectionConfiguration(collection: Collection): CollectionConfiguration?

    /**
     * The list of collections in the replicator configuration
     */
    public val collections: Set<Collection>

    /**
     * Replicator type indicating the direction of the replicator.
     */
    public var type: ReplicatorType

    /**
     * The continuous flag indicating whether the replicator should stay
     * active indefinitely to replicate changed documents.
     */
    public var isContinuous: Boolean

    /**
     * Enable/disable auto-purge.
     * Default is enabled.
     *
     * Note: A document that is blocked by a document Id filter will not be auto-purged
     * regardless of the setting of the auto purge property
     */
    public var isAutoPurgeEnabled: Boolean

    /**
     * Return Extra HTTP headers to send in all requests to the remote target.
     */
    public var headers: Map<String, String>?

    /**
     * The option to remove a restriction that does not allow a replicator to accept cookies
     * from a remote host unless the cookie domain exactly matches the domain of the sender.
     * For instance, when the option is set to false (the default), and the remote host, “bar.foo.com”,
     * sends a cookie for the domain “.foo.com”, the replicator will reject it. If the option
     * is set true, however, the replicator will accept it. This is, in general, dangerous:
     * a host might, for instance, set a cookie for the domain ".com". It is safe only when
     * the replicator is connecting only to known hosts.
     * The default value of this option is false: parent-domain cookies are not accepted
     */
    public var isAcceptParentDomainCookies: Boolean

    /**
     * The Authenticator used to authenticate the remote.
     */
    public var authenticator: Authenticator?

    /**
     * The remote target's SSL certificate.
     */
    public var pinnedServerCertificate: ByteArray?

    /**
     * The max number of retry attempts made after connection failure.
     * This method will return 0 when implicitly using the default:
     * 10 total connection attempts (the initial attempt and up to 9 retries) for
     * a one-shot replicator and a very, very large number of retries, for a continuous replicator.
     */
    public var maxAttempts: Int

    /**
     * The max time between retry attempts (exponential backoff).
     */
    public var maxAttemptWaitTime: Int

    /**
     * The heartbeat interval, in seconds.
     */
    public var heartbeat: Int

    /**
     * The local database to replicate with the replication target.
     */
    @Deprecated("Use Collection.database")
    public val database: Database

    /**
     * A collection of document IDs to filter: if not null, only documents with these IDs will be pushed
     * and/or pulled.
     */
    @Deprecated("Use CollectionConfiguration.documentIDs")
    public var documentIDs: List<String>?

    /**
     * Gets the collection of Sync Gateway channel names from which to pull documents.
     * If unset, all accessible channels will be pulled.
     * Default is empty: pull from all accessible channels.
     *
     * Note:  Channel specifications apply only to replications
     * pulling from a SyncGateway and only the channels visible
     * to the authenticated user.  Channel specs are ignored:
     *
     *  * during a push replication.
     *  * during peer-to-peer or database-to-database replication
     *  * when the specified channel is not accessible to the user
     */
    @Deprecated("Use CollectionConfiguration.channels")
    public var channels: List<String>?

    /**
     * The conflict resolver.
     */
    @Deprecated("Use CollectionConfiguration.conflictResolver")
    public var conflictResolver: ConflictResolver?

    /**
     * The filter used to determine whether a document will be pulled
     * from the remote endpoint.
     */
    @Deprecated("Use CollectionConfiguration.pullFilter")
    public var pullFilter: ReplicationFilter?

    /**
     * The filter used to determine whether a document will be pushed
     * to the remote endpoint.
     */
    @Deprecated("Use CollectionConfiguration.pushFilter")
    public var pushFilter: ReplicationFilter?

    public companion object
}

/**
 * This is a long time: just under 25 days.
 * This many seconds, however, is just less than Integer.MAX_INT millis and will fit in the heartbeat property.
 */
public val ReplicatorConfiguration.Companion.DISABLE_HEARTBEAT: Int
    get() = 2147483

/**
 * The default conflict resolution strategy.
 * Deletion always wins.  A newer doc always beats an older one.
 * Otherwise one of the two document is chosen randomly but deterministically.
 */
public val ReplicatorConfiguration.Companion.DEFAULT_CONFLICT_RESOLVER: ConflictResolver
    get() = defaultConflictResolver

private val defaultConflictResolver: ConflictResolver by lazy {
    cr@{ conflict ->
        // deletion always wins.
        val localDoc = conflict.localDocument
        val remoteDoc = conflict.remoteDocument
        if (localDoc == null || remoteDoc == null) return@cr null

        // if one of the docs is newer, return it
        val localGen = localDoc.generation
        val remoteGen = remoteDoc.generation
        if (localGen > remoteGen) {
            return@cr localDoc
        } else if (localGen < remoteGen) {
            return@cr remoteDoc
        }

        // otherwise, choose one randomly, but deterministically.
        val localRevId = localDoc.revisionID ?: return@cr remoteDoc
        val remoteRevId = remoteDoc.revisionID
        return@cr if (remoteRevId == null || localRevId < remoteRevId) remoteDoc else localDoc
    }
}
