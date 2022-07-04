package com.couchbase.lite.kmm

/**
 * Configuration factory for new ReplicatorConfigurations
 * Usage:
 *     val replConfig = ReplicatorConfigurationFactory.create(...)
 */
public val ReplicatorConfigurationFactory: ReplicatorConfiguration? = null

/**
 * Create a FullTextIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param database (required) the local database.
 * @param target (required) The max size of the log file in bytes.
 * @param type replicator type: push, pull, or push and pull: default is push and pull.
 * @param continuous continuous flag: true for continuous, false by default.
 * @param authenticator connection authenticator.
 * @param headers extra HTTP headers to send in all requests to the remote target.
 * @param pinnedServerCertificate target server's SSL certificate.
 * @param channels Sync Gateway channel names.
 * @param documentIDs IDs of documents to be replicated: default is all documents.
 * @param pushFilter filter for pushed documents.
 * @param pullFilter filter for pulled documents.
 * @param conflictResolver conflict resolver.
 * @param maxAttempts max retry attempts after connection failure.
 * @param maxAttemptWaitTime max time between retry attempts (exponential backoff).
 * @param heartbeat heartbeat interval, in seconds.
 * @param enableAutoPurge auto-purge enabled.
 *
 * @see com.couchbase.lite.kmm.ReplicatorConfiguration
 */
public fun ReplicatorConfiguration?.create(
    database: Database? = null,
    target: Endpoint? = null,
    type: ReplicatorType? = null,
    continuous: Boolean? = null,
    authenticator: Authenticator? = null,
    headers: Map<String, String>? = null,
    pinnedServerCertificate: ByteArray? = null,
    channels: List<String>? = null,
    documentIDs: List<String>? = null,
    pushFilter: ReplicationFilter? = null,
    pullFilter: ReplicationFilter? = null,
    conflictResolver: ConflictResolver? = null,
    maxAttempts: Int? = null,
    maxAttemptWaitTime: Int? = null,
    heartbeat: Int? = null,
    enableAutoPurge: Boolean? = null
): ReplicatorConfiguration {
    val replicatorConfiguration = if (this != null) {
        ReplicatorConfiguration(this)
    } else {
        ReplicatorConfiguration(
            database ?: error("Must specify a database"),
            target ?: error("Must specify a target")
        )
    }
    return replicatorConfiguration.apply {
        type?.let { this.type = it }
        continuous?.let { this.isContinuous = it }
        authenticator?.let { this.authenticator = it }
        headers?.let { this.headers = it }
        pinnedServerCertificate?.let { this.pinnedServerCertificate = it }
        channels?.let { this.channels = it }
        documentIDs?.let { this.documentIDs = it }
        pushFilter?.let { this.pushFilter = it }
        pullFilter?.let { this.pullFilter = it }
        conflictResolver?.let { this.conflictResolver = it }
        maxAttempts?.let { this.maxAttempts = it }
        maxAttemptWaitTime?.let { this.maxAttemptWaitTime = it }
        heartbeat?.let { this.heartbeat = it }
        enableAutoPurge?.let { this.isAutoPurgeEnabled = it }
    }
}

public val DatabaseConfigurationFactory: DatabaseConfiguration? = null
public fun DatabaseConfiguration?.create(databasePath: String? = null): DatabaseConfiguration {
    return DatabaseConfiguration(this).apply {
        databasePath?.let { setDirectory(it) }
    }
}
