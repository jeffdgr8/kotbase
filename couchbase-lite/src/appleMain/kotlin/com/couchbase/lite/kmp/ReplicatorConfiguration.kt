package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.*
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toByteArray
import com.udobny.kmp.ext.toCFData
import kotlinx.cinterop.convert
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateCreateWithData

public actual class ReplicatorConfiguration
private constructor(
    public actual val database: Database,
    public actual val target: Endpoint,
    actual: CBLReplicatorConfiguration,
    authenticator: Authenticator? = null,
    conflictResolver: ConflictResolver? = null,
    pullFilter: ReplicationFilter? = null,
    pushFilter: ReplicationFilter? = null
) : DelegatedClass<CBLReplicatorConfiguration>(actual) {

    public actual constructor(database: Database, target: Endpoint) : this(
        database,
        target,
        CBLReplicatorConfiguration(database.actual, target.actual)
    )

    public actual constructor(config: ReplicatorConfiguration) : this(
        config.database,
        config.target,
        CBLReplicatorConfiguration(config.actual),
        config.authenticator,
        config.conflictResolver,
        config.pullFilter,
        config.pushFilter
    )

    public actual fun setAuthenticator(authenticator: Authenticator): ReplicatorConfiguration =
        chain {
            this@ReplicatorConfiguration.authenticator = authenticator
        }

    public actual fun setChannels(channels: List<String>?): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.channels = channels
    }

    public actual fun setConflictResolver(conflictResolver: ConflictResolver?): ReplicatorConfiguration =
        chain {
            this@ReplicatorConfiguration.conflictResolver = conflictResolver
        }

    public actual fun setContinuous(continuous: Boolean): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.isContinuous = continuous
    }

    public actual fun setDocumentIDs(documentIDs: List<String>?): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.documentIDs = documentIDs
    }

    public actual fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.headers = headers
    }

    public actual fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration =
        chain {
            this@ReplicatorConfiguration.pinnedServerCertificate = pinnedCert
        }

    public actual fun setPullFilter(pullFilter: ReplicationFilter?): ReplicatorConfiguration =
        chain {
            this@ReplicatorConfiguration.pullFilter = pullFilter
        }

    public actual fun setPushFilter(pushFilter: ReplicationFilter?): ReplicatorConfiguration =
        chain {
            this@ReplicatorConfiguration.pushFilter = pushFilter
        }

    public actual fun setType(type: ReplicatorType): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.type = type
    }

    public actual fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.maxAttempts = maxAttempts
    }

    public actual fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration =
        chain {
            this@ReplicatorConfiguration.maxAttemptWaitTime = maxAttemptWaitTime
        }

    public actual fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.heartbeat = heartbeat
    }

    public actual fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration = chain {
        this@ReplicatorConfiguration.isAutoPurgeEnabled = enabled
    }

    public actual var authenticator: Authenticator? = authenticator
        set(value) {
            field = value
            actual.authenticator = value?.actual
        }

    public actual var channels: List<String>?
        @Suppress("UNCHECKED_CAST")
        get() = actual.channels as List<String>?
        set(value) {
            actual.channels = value
        }

    public actual var conflictResolver: ConflictResolver? = conflictResolver
        set(value) {
            field = value
            actual.conflictResolver = value?.convert()
        }

    public actual var isContinuous: Boolean
        get() = actual.continuous
        set(value) {
            actual.continuous = value
        }

    public actual var documentIDs: List<String>?
        @Suppress("UNCHECKED_CAST")
        get() = actual.documentIDs as List<String>?
        set(value) {
            actual.documentIDs = value
        }

    @Suppress("UNCHECKED_CAST")
    public actual var headers: Map<String, String>?
        get() = actual.headers as Map<String, String>?
        set(value) {
            actual.headers = value as Map<Any?, *>
        }

    public actual var pinnedServerCertificate: ByteArray?
        get() = SecCertificateCopyData(actual.pinnedServerCertificate)?.toByteArray()
        set(value) {
            val cert = SecCertificateCreateWithData(null, value?.toCFData())
            actual.pinnedServerCertificate = cert
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
        get() = ReplicatorType.from(actual.replicatorType)
        set(value) {
            actual.replicatorType = value.actual
        }

    public actual var maxAttempts: Int
        get() = actual.maxAttempts.toInt()
        set(value) {
            actual.maxAttempts = value.convert()
        }

    public actual var maxAttemptWaitTime: Int
        get() = actual.maxAttemptWaitTime.toInt()
        set(value) {
            actual.maxAttemptWaitTime = value.toDouble()
        }

    public actual var heartbeat: Int
        get() = actual.heartbeat.toInt()
        set(value) {
            actual.heartbeat = value.toDouble()
        }

    public actual var isAutoPurgeEnabled: Boolean
        get() = actual.enableAutoPurge
        set(value) {
            actual.enableAutoPurge = value
        }

    public actual companion object
}
