package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.*
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toByteArray
import com.udobny.kmm.ext.toCFData
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateCreateWithData

public actual class ReplicatorConfiguration
internal constructor(actual: CBLReplicatorConfiguration) :
    DelegatedClass<CBLReplicatorConfiguration>(actual) {

    public actual constructor(database: Database, target: Endpoint) : this(
        CBLReplicatorConfiguration(database.actual, target.actual)
    )

    public actual constructor(config: ReplicatorConfiguration) : this(
        CBLReplicatorConfiguration(config.actual)
    )

    public actual var authenticator: Authenticator?
        get() = actual.authenticator?.toAuthenticator()
        set(value) {
            actual.authenticator = value?.actual
        }

    public actual var channels: List<String>?
        @Suppress("UNCHECKED_CAST")
        get() = actual.channels as List<String>?
        set(value) {
            actual.channels = value
        }

    public actual var conflictResolver: ConflictResolver?
        get() = (actual.conflictResolver as DelegatedConflictResolver?)?.actual
        set(value) {
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

    public actual var pullFilter: ReplicationFilter? = null
        get() = field ?: actual.pullFilter?.convert()
        set(value) {
            field = value
            actual.pullFilter = value?.convert()
        }

    public actual var pushFilter: ReplicationFilter? = null
        get() = field ?: actual.pushFilter?.convert()
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
            actual.maxAttempts = value.toULong()
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

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val target: Endpoint by lazy {
        actual.target.asEndpoint()
    }
}
