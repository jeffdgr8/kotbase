package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class ReplicatorConfiguration
internal constructor(actual: com.couchbase.lite.ReplicatorConfiguration) :
    DelegatedClass<com.couchbase.lite.ReplicatorConfiguration>(actual) {

    public actual constructor(database: Database, target: Endpoint) : this(
        com.couchbase.lite.ReplicatorConfiguration(database.actual, target.actual)
    )

    public actual constructor(config: ReplicatorConfiguration) : this(
        com.couchbase.lite.ReplicatorConfiguration(config.actual)
    )

    public actual var authenticator: Authenticator?
        get() = actual.authenticator?.toAuthenticator()
        set(value) {
            actual.setAuthenticator(value!!.actual)
        }

    public actual var channels: List<String>?
        get() = actual.channels
        set(value) {
            actual.channels = value
        }

    public actual var conflictResolver: ConflictResolver?
        get() = (actual.conflictResolver as DelegatedConflictResolver?)?.actual
        set(value) {
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

    public actual var pullFilter: ReplicationFilter?
        get() = (actual.pullFilter as DelegatedReplicationFilter?)?.actual
        set(value) {
            actual.pullFilter = value?.convert()
        }

    public actual var pushFilter: ReplicationFilter?
        get() = (actual.pushFilter as DelegatedReplicationFilter?)?.actual
        set(value) {
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

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val target: Endpoint by lazy {
        actual.target.asEndpoint()
    }
}
