package kotbase

import kotbase.base.DelegatedClass

public actual class ReplicatorConfiguration
private constructor(
    public actual val database: Database,
    public actual val target: Endpoint,
    actual: com.couchbase.lite.ReplicatorConfiguration,
    authenticator: Authenticator? = null,
    conflictResolver: ConflictResolver? = null,
    pullFilter: ReplicationFilter? = null,
    pushFilter: ReplicationFilter? = null
) : DelegatedClass<com.couchbase.lite.ReplicatorConfiguration>(actual) {

    public actual constructor(database: Database, target: Endpoint) : this(
        database,
        target,
        com.couchbase.lite.ReplicatorConfiguration(database.actual, target.actual)
    )

    public actual constructor(config: ReplicatorConfiguration) : this(
        config.database,
        config.target,
        com.couchbase.lite.ReplicatorConfiguration(config.actual),
        config.authenticator,
        config.conflictResolver,
        config.pullFilter,
        config.pushFilter
    )

    public actual fun setAuthenticator(authenticator: Authenticator): ReplicatorConfiguration {
        this@ReplicatorConfiguration.authenticator = authenticator
        return this
    }

    public actual fun setChannels(channels: List<String>?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.channels = channels
        return this
    }

    public actual fun setConflictResolver(conflictResolver: ConflictResolver?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.conflictResolver = conflictResolver
        return this
    }

    public actual fun setContinuous(continuous: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isContinuous = continuous
        return this
    }

    public actual fun setDocumentIDs(documentIDs: List<String>?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.documentIDs = documentIDs
        return this
    }

    public actual fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.headers = headers
        return this
    }

    public actual fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.pinnedServerCertificate = pinnedCert
        return this
    }

    public actual fun setPullFilter(pullFilter: ReplicationFilter?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.pullFilter = pullFilter
        return this
    }

    public actual fun setPushFilter(pushFilter: ReplicationFilter?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.pushFilter = pushFilter
        return this
    }

    public actual fun setType(type: ReplicatorType): ReplicatorConfiguration {
        this@ReplicatorConfiguration.type = type
        return this
    }

    public actual fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration {
        this@ReplicatorConfiguration.maxAttempts = maxAttempts
        return this
    }

    public actual fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration {
        this@ReplicatorConfiguration.maxAttemptWaitTime = maxAttemptWaitTime
        return this
    }

    public actual fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration {
        this@ReplicatorConfiguration.heartbeat = heartbeat
        return this
    }

    public actual fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isAutoPurgeEnabled = enabled
        return this
    }

    public actual var authenticator: Authenticator? = authenticator
        set(value) {
            field = value
            actual.setAuthenticator(value!!.actual)
        }

    public actual var channels: List<String>?
        get() = actual.channels
        set(value) {
            actual.channels = value
        }

    public actual var conflictResolver: ConflictResolver? = conflictResolver
        set(value) {
            field = value
            actual.conflictResolver = value?.convert()
        }

    public actual var isContinuous: Boolean
        get() = actual.isContinuous
        set(value) {
            actual.isContinuous = value
        }

    public actual var documentIDs: List<String>?
        get() = actual.documentIDs
        set(value) {
            actual.documentIDs = value
        }

    public actual var headers: Map<String, String>?
        get() = actual.headers
        set(value) {
            actual.headers = value
        }

    public actual var pinnedServerCertificate: ByteArray?
        get() = actual.pinnedServerCertificate
        set(value) {
            actual.pinnedServerCertificate = value
        }

    public actual var pullFilter: ReplicationFilter? = pullFilter
        set(value) {
            field = value
            actual.pullFilter = value?.convert()
        }

    public actual var pushFilter: ReplicationFilter? = pushFilter
        set(value) {
            field = value
            actual.pushFilter = value?.convert()
        }

    public actual var type: ReplicatorType
        get() = ReplicatorType.from(actual.type)
        set(value) {
            actual.type = value.actual
        }

    public actual var maxAttempts: Int
        get() = actual.maxAttempts
        set(value) {
            actual.maxAttempts = value
        }

    public actual var maxAttemptWaitTime: Int
        get() = actual.maxAttemptWaitTime
        set(value) {
            actual.maxAttemptWaitTime = value
        }

    public actual var heartbeat: Int
        get() = actual.heartbeat
        set(value) {
            actual.heartbeat = value
        }

    public actual var isAutoPurgeEnabled: Boolean
        get() = actual.isAutoPurgeEnabled
        set(value) {
            actual.isAutoPurgeEnabled = value
        }

    public actual companion object
}
