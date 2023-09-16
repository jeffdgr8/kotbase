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

import cocoapods.CouchbaseLite.CBLReplicatorConfiguration
import kotbase.base.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toSecCertificate
import kotlinx.cinterop.convert

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

    public actual fun setAcceptParentDomainCookies(acceptParentCookies: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isAcceptParentDomainCookies = acceptParentCookies
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

    public actual var isAcceptParentDomainCookies: Boolean
        get() = actual.acceptParentDomainCookies
        set(value) {
            actual.acceptParentDomainCookies = value
        }

    public actual var pinnedServerCertificate: ByteArray?
        get() = actual.pinnedServerCertificate?.toByteArray()
        set(value) {
            actual.pinnedServerCertificate = value?.toSecCertificate()
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
