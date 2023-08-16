package kotbase

import com.couchbase.lite.generation

/**
 * Configuration for a Replicator
 */
public expect class ReplicatorConfiguration {

    public constructor(database: Database, target: Endpoint)

    public constructor(config: ReplicatorConfiguration)

    /**
     * Sets the authenticator to authenticate with a remote target server.
     * Currently there are two types of the authenticators,
     * BasicAuthenticator and SessionAuthenticator, supported.
     *
     * @param authenticator The authenticator.
     * @return this.
     */
    public fun setAuthenticator(authenticator: Authenticator): ReplicatorConfiguration

    /**
     * Sets a set of Sync Gateway channel names to pull from. Ignored for
     * push replication. If unset, all accessible channels will be pulled.
     * Note: channels that are not accessible to the user will be ignored
     * by Sync Gateway.
     *
     * @param channels The Sync Gateway channel names.
     * @return this.
     */
    public fun setChannels(channels: List<String>?): ReplicatorConfiguration

    /**
     * Sets the conflict resolver.
     *
     * @param conflictResolver A conflict resolver.
     * @return this.
     */
    public fun setConflictResolver(conflictResolver: ConflictResolver?): ReplicatorConfiguration

    /**
     * Sets whether the replicator stays active indefinitely to replicate
     * changed documents. The default value is false, which means that the
     * replicator will stop after it finishes replicating the changed
     * documents.
     *
     * @param continuous The continuous flag.
     * @return this.
     */
    public fun setContinuous(continuous: Boolean): ReplicatorConfiguration

    /**
     * Sets a set of document IDs to filter by: if given, only documents
     * with these IDs will be pushed and/or pulled.
     *
     * @param documentIDs The document IDs.
     * @return this.
     */
    public fun setDocumentIDs(documentIDs: List<String>?): ReplicatorConfiguration

    /**
     * Sets the extra HTTP headers to send in all requests to the remote target.
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
     * Sets the target server's SSL certificate.
     *
     * @param pinnedCert the SSL certificate.
     * @return this.
     */
    public fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration

    /**
     * Sets a filter object for validating whether the documents can be pulled from the
     * remote endpoint. Only documents for which the object returns true are replicated.
     *
     * @param pullFilter The filter to filter the document to be pulled.
     * @return this.
     */
    public fun setPullFilter(pullFilter: ReplicationFilter?): ReplicatorConfiguration

    /**
     * Sets a filter object for validating whether the documents can be pushed
     * to the remote endpoint.
     *
     * @param pushFilter The filter to filter the document to be pushed.
     * @return this.
     */
    public fun setPushFilter(pushFilter: ReplicationFilter?): ReplicatorConfiguration

    /**
     * Sets the replicator type indicating the direction of the replicator.
     * The default value is .pushAndPull which is bi-directional.
     *
     * @param type The replicator type.
     * @return this.
     */
    public fun setType(type: ReplicatorType): ReplicatorConfiguration

    /**
     * Set the max number of retry attempts made after a connection failure.
     * Set to 0 for default values.
     * Set to 1 for no retries.
     *
     * @param maxAttempts max retry attempts
     */
    public fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration

    /**
     * Set the max time between retry attempts (exponential backoff).
     * Set to 0 for default values.
     *
     * @param maxAttemptWaitTime max attempt wait time
     */
    public fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration

    /**
     * Set the heartbeat interval, in seconds.
     * Set to 0 for default values
     *
     * Must be non-negative and less than Integer.MAX_VALUE milliseconds
     */
    public fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration

    /**
     * Enable/disable auto-purge.
     *
     * Auto-purge is enabled, by default.
     *
     * When the autoPurge flag is disabled, the replicator will notify the registered DocumentReplication listeners
     * with an "access removed" event when access to the document is revoked on the Sync Gateway. On receiving the
     * event, the application may decide to manually purge the document. However, for performance reasons, any
     * DocumentReplication listeners added to the replicator after the replicator is started will not receive the
     * access removed events until the replicator is restarted or reconnected with Sync Gateway.
     */
    public fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration

    /**
     * The Authenticator to authenticate with a remote target.
     */
    public var authenticator: Authenticator?

    /**
     * A set of Sync Gateway channel names to pull from. Ignored for push replication.
     * The default value is null, meaning that all accessible channels will be pulled.
     * Note: channels that are not accessible to the user will be ignored by Sync Gateway.
     */
    public var channels: List<String>?

    /**
     * Return the conflict resolver.
     */
    public var conflictResolver: ConflictResolver?

    /**
     * Return the continuous flag indicating whether the replicator should stay
     * active indefinitely to replicate changed documents.
     */
    public var isContinuous: Boolean

    /**
     * Return the local database to replicate with the replication target.
     */
    public val database: Database

    /**
     * A set of document IDs to filter by: if not nil, only documents with these IDs will be pushed
     * and/or pulled.
     */
    public var documentIDs: List<String>?

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
     * Return the remote target's SSL certificate.
     */
    public var pinnedServerCertificate: ByteArray?

    /**
     * Gets a filter object for validating whether the documents can be pulled
     * from the remote endpoint.
     */
    public var pullFilter: ReplicationFilter?

    /**
     * Gets a filter object for validating whether the documents can be pushed
     * to the remote endpoint.
     */
    public var pushFilter: ReplicationFilter?

    /**
     * Return Replicator type indicating the direction of the replicator.
     */
    public var type: ReplicatorType

    /**
     * Return the replication target to replicate with.
     */
    public val target: Endpoint

    /**
     * Return the max number of retry attempts made after connection failure.
     */
    public var maxAttempts: Int

    /**
     * Return the max time between retry attempts (exponential backoff).
     *
     * @return max retry wait time
     */
    public var maxAttemptWaitTime: Int

    /**
     * Return the heartbeat interval, in seconds.
     *
     * @return heartbeat interval in seconds
     */
    public var heartbeat: Int

    /**
     * Enable/disable auto-purge.
     * Default is enabled.
     */
    public var isAutoPurgeEnabled: Boolean

    public companion object
}

/**
 * This is a long time: just under 25 days.
 * This many seconds, however, is just less than Integer.MAX_INT millis, and will fit in the heartbeat property.
 */
public val ReplicatorConfiguration.Companion.DISABLE_HEARTBEAT: Int
    get() = 2147483

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
        val remoteGen: Long = remoteDoc.generation
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
