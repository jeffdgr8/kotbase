package com.couchbase.lite.kmp

/**
 * Replicator configuration.
 */
public expect class ReplicatorConfiguration {

    public constructor(database: Database, target: Endpoint)

    public constructor(config: ReplicatorConfiguration)

    /**
     * Sets the authenticator to authenticate with a remote target server.
     * Currently there are two types of the authenticators,
     * BasicAuthenticator and SessionAuthenticator, supported.
     */
    public var authenticator: Authenticator?

    /**
     * Sets a set of Sync Gateway channel names to pull from. Ignored for
     * push replication. If unset, all accessible channels will be pulled.
     * Note: channels that are not accessible to the user will be ignored
     * by Sync Gateway.
     */
    public var channels: List<String>?

    /**
     * Sets the the conflict resolver.
     */
    public var conflictResolver: ConflictResolver?

    /**
     * Sets whether the replicator stays active indefinitely to replicate
     * changed documents. The default value is false, which means that the
     * replicator will stop after it finishes replicating the changed
     * documents.
     */
    public var isContinuous: Boolean

    /**
     * Sets a set of document IDs to filter by: if given, only documents
     * with these IDs will be pushed and/or pulled.
     */
    public var documentIDs: List<String>?

    /**
     * Sets the extra HTTP headers to send in all requests to the remote target.
     */
    public var headers: Map<String, String>?

    /**
     * Sets the target server's SSL certificate.
     */
    public var pinnedServerCertificate: ByteArray?

    /**
     * Sets a filter object for validating whether the documents can be pulled from the
     * remote endpoint. Only documents for which the object returns true are replicated.
     */
    public var pullFilter: ReplicationFilter?

    /**
     * Sets a filter object for validating whether the documents can be pushed
     * to the remote endpoint.
     */
    public var pushFilter: ReplicationFilter?

    /**
     * Sets the replicator type indicating the direction of the replicator.
     * The default value is .pushAndPull which is bi-directional.
     */
    public var type: ReplicatorType

    /**
     * Set the max number of retry attempts made after a connection failure.
     * Set to 0 for default values.
     * Set to 1 for no retries.
     */
    public var maxAttempts: Int

    /**
     * Set the max time between retry attempts (exponential backoff).
     * Set to 0 for default values.
     */
    public var maxAttemptWaitTime: Int

    /**
     * Set the heartbeat interval, in seconds.
     * Set to 0 for default values
     *
     * Must be non-negative and less than Integer.MAX_VALUE milliseconds
     */
    public var heartbeat: Int

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
    public var isAutoPurgeEnabled: Boolean

    /**
     * The local database to replicate with the replication target.
     */
    public val database: Database

    /**
     * The replication target to replicate with.
     */
    public val target: Endpoint
}

/**
 * This is a long time: just under 25 days.
 * This many seconds, however, is just less than Integer.MAX_INT millis, and will fit in the heartbeat property.
 */
public const val DISABLE_HEARTBEAT: Int = 2147483
