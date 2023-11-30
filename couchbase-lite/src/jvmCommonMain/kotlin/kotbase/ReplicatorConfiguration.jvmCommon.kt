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

import kotbase.internal.DelegatedClass
import kotbase.internal.actuals
import java.security.cert.X509Certificate
import com.couchbase.lite.ReplicatorConfiguration as CBLReplicatorConfiguration

public actual class ReplicatorConfiguration
private constructor(
    actual: CBLReplicatorConfiguration,
    public actual val target: Endpoint,
    private var db: Database? = null,
    private val collectionConfigurations: MutableMap<Collection, CollectionConfiguration> = mutableMapOf(),
    authenticator: Authenticator? = null
) : DelegatedClass<CBLReplicatorConfiguration>(actual) {

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use ReplicatorConfiguration(Endpoint)",
        ReplaceWith("ReplicatorConfiguration(target)")
    )
    public actual constructor(database: Database, target: Endpoint) : this(
        CBLReplicatorConfiguration(database.actual, target.actual),
        target,
        database
    ) {
        addCollection(database.getDefaultCollectionNotNull(), null)
    }

    public actual constructor(target: Endpoint) : this(
        CBLReplicatorConfiguration(target.actual),
        target
    )

    public actual constructor(config: ReplicatorConfiguration) : this(
        CBLReplicatorConfiguration(config.actual),
        config.target,
        config.db,
        config.collectionConfigurations.toMutableMap(),
        config.authenticator
    )

    public actual fun addCollection(collection: Collection, config: CollectionConfiguration?): ReplicatorConfiguration {
        val configNotNull = config?.let(::CollectionConfiguration) ?: CollectionConfiguration()
        actual.addCollection(collection.actual, configNotNull.actual)
        collectionConfigurations[collection] = configNotNull
        return this
    }

    public actual fun addCollections(
        collections: kotlin.collections.Collection<Collection>,
        config: CollectionConfiguration?
    ): ReplicatorConfiguration {
        val configNotNull = config?.let(::CollectionConfiguration) ?: CollectionConfiguration()
        actual.addCollections(collections.actuals(), configNotNull.actual)
        collections.forEach {
            collectionConfigurations[it] = configNotNull
        }
        return this
    }

    public actual fun removeCollection(collection: Collection): ReplicatorConfiguration {
        actual.removeCollection(collection.actual)
        collectionConfigurations.remove(collection)
        return this
    }

    public actual fun setType(type: ReplicatorType): ReplicatorConfiguration {
        this@ReplicatorConfiguration.type = type
        return this
    }

    public actual fun setContinuous(continuous: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isContinuous = continuous
        return this
    }

    public actual fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isAutoPurgeEnabled = enabled
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

    public actual fun setAuthenticator(authenticator: Authenticator?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.authenticator = authenticator
        return this
    }

    public actual fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.pinnedServerCertificate = pinnedCert
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

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setDocumentIDs")
    public actual fun setDocumentIDs(documentIDs: List<String>?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.documentIDs = documentIDs
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setChannels")
    public actual fun setChannels(channels: List<String>?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.channels = channels
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setConflictResolver")
    public actual fun setConflictResolver(conflictResolver: ConflictResolver?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.conflictResolver = conflictResolver
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setPullFilter")
    public actual fun setPullFilter(pullFilter: ReplicationFilter?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.pullFilter = pullFilter
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setPushFilter")
    public actual fun setPushFilter(pushFilter: ReplicationFilter?): ReplicatorConfiguration {
        this@ReplicatorConfiguration.pushFilter = pushFilter
        return this
    }

    public actual fun getCollectionConfiguration(collection: Collection): CollectionConfiguration? =
        collectionConfigurations[collection]?.let(::CollectionConfiguration)

    public actual val collections: Set<Collection>
        get() = collectionConfigurations.keys

    public actual var type: ReplicatorType
        get() = ReplicatorType.from(actual.type)
        set(value) {
            actual.type = value.actual
        }

    public actual var isContinuous: Boolean
        get() = actual.isContinuous
        set(value) {
            actual.isContinuous = value
        }

    public actual var isAutoPurgeEnabled: Boolean
        get() = actual.isAutoPurgeEnabled
        set(value) {
            actual.isAutoPurgeEnabled = value
        }

    public actual var headers: Map<String, String>?
        get() = actual.headers
        set(value) {
            actual.headers = value
        }

    public actual var isAcceptParentDomainCookies: Boolean
        get() = actual.isAcceptParentDomainCookies
        set(value) {
            actual.isAcceptParentDomainCookies = value
        }

    public actual var authenticator: Authenticator? = authenticator
        set(value) {
            field = value
            actual.setAuthenticator(value?.actual)
        }

    @Suppress("DEPRECATION")
    public actual var pinnedServerCertificate: ByteArray?
        get() = actual.pinnedServerCertificate
        set(value) {
            actual.pinnedServerCertificate = value
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

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.collections")
    public actual val database: Database
        get() {
            val actualDb = actual.database
            return collectionConfigurations.keys.firstOrNull()?.database
                ?: db
                ?: Database(actualDb)
        }

    @Deprecated("Use CollectionConfiguration.documentIDs")
    public actual var documentIDs: List<String>?
        get() = getDefaultCollectionConfiguration().documentIDs?.toList()
        set(value) {
            updateDefaultConfig {
                documentIDs = value
            }
        }

    @Deprecated("Use CollectionConfiguration.channels")
    public actual var channels: List<String>?
        get() = getDefaultCollectionConfiguration().channels?.toList()
        set(value) {
            updateDefaultConfig {
                channels = value
            }
        }

    @Deprecated("Use CollectionConfiguration.conflictResolver")
    public actual var conflictResolver: ConflictResolver?
        get() = getDefaultCollectionConfiguration().conflictResolver
        set(value) {
            updateDefaultConfig {
                conflictResolver = value
            }
        }

    @Deprecated("Use CollectionConfiguration.pullFilter")
    public actual var pullFilter: ReplicationFilter?
        get() = getDefaultCollectionConfiguration().pullFilter
        set(value) {
            updateDefaultConfig {
                pullFilter = value
            }
        }

    @Deprecated("Use CollectionConfiguration.pushFilter")
    public actual var pushFilter: ReplicationFilter?
        get() = getDefaultCollectionConfiguration().pushFilter
        set(value) {
            updateDefaultConfig {
                pushFilter = value
            }
        }

    @Suppress("DEPRECATION")
    private val defaultCollection: Collection by lazy {
        database.getDefaultCollection()
            ?: throw IllegalArgumentException("Cannot use legacy parameters when there is no default collection")
    }

    private fun getDefaultCollectionConfiguration(): CollectionConfiguration {
        return collectionConfigurations[defaultCollection]
            ?: throw IllegalArgumentException(
                "Cannot use legacy parameters when the default collection has no configuration"
            )
    }

    private fun updateDefaultConfig(updater: CollectionConfiguration.() -> Unit) {
        val config = getDefaultCollectionConfiguration()
        val updated = CollectionConfiguration(config)
        updated.updater()
        addCollection(defaultCollection, updated)
    }

    public actual companion object
}

/**
 * Sets the certificate used to authenticate the target server.
 * A server will be authenticated if it presents a chain of certificates (possibly of length 1)
 * in which any one of the certificates matches the one passed here.
 * The default is no pinned certificate.
 *
 * @param pinnedCert the SSL certificate.
 * @return this.
 */
public fun ReplicatorConfiguration.setPinnedServerX509Certificate(
    pinnedCert: X509Certificate?
): ReplicatorConfiguration {
    actual.setPinnedServerX509Certificate(pinnedCert)
    return this
}

/**
 * The remote target's SSL certificate.
 */
public var ReplicatorConfiguration.pinnedServerX509Certificate: X509Certificate?
    get() = actual.pinnedServerX509Certificate
    set(value) {
        actual.setPinnedServerX509Certificate(value)
    }
