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
import cocoapods.CouchbaseLite.isClosed
import kotbase.ext.toByteArray
import kotbase.ext.toSecCertificate
import kotbase.internal.DelegatedClass
import kotlinx.cinterop.convert
import platform.Security.SecCertificateRef

public actual class ReplicatorConfiguration
private constructor(
    actual: CBLReplicatorConfiguration,
    public actual val target: Endpoint,
    private var db: Database? = null,
    private val collectionConfigurations: MutableMap<Collection, CollectionConfiguration> = mutableMapOf(),
    authenticator: Authenticator? = null
) : DelegatedClass<CBLReplicatorConfiguration>(actual) {

    @Deprecated(
        "Use ReplicatorConfiguration(Endpoint)",
        ReplaceWith("ReplicatorConfiguration(target)")
    )
    public actual constructor(database: Database, target: Endpoint) : this(
        CBLReplicatorConfiguration(database.actual, target.actual),
        target,
        database
    ) {
        addCollection(database.getDefaultCollection(), null)
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

    private fun checkCollection(collection: Collection) {
        val database = collectionConfigurations.keys.firstOrNull()?.database ?: db ?: collection.database
        if (database != collection.database) {
            throw IllegalArgumentException("Cannot add collection $collection because it does not belong to database ${database.name}.")
        }
        if (database.actual.isClosed()) {
            throw IllegalArgumentException("Cannot add collection $collection because database ${collection.database} is closed.")
        }
        try {
            database.getCollection(collection.name, collection.scope.name)
                ?: throw IllegalArgumentException("Cannot add collection $collection because it has been deleted.")
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed getting collection $collection", e)
        }
    }

    public actual fun addCollection(collection: Collection, config: CollectionConfiguration?): ReplicatorConfiguration {
        checkCollection(collection)
        val configNotNull = config?.let(::CollectionConfiguration) ?: CollectionConfiguration()
        collectionConfigurations[collection] = configNotNull
        actual.addCollection(collection.actual, configNotNull.actual)
        return this
    }

    public actual fun addCollections(
        collections: kotlin.collections.Collection<Collection>,
        config: CollectionConfiguration?
    ): ReplicatorConfiguration {
        collections.forEach { collection ->
            addCollection(collection, config)
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
        get() = ReplicatorType.from(actual.replicatorType)
        set(value) {
            actual.replicatorType = value.actual
        }

    public actual var isContinuous: Boolean
        get() = actual.continuous
        set(value) {
            actual.continuous = value
        }

    public actual var isAutoPurgeEnabled: Boolean
        get() = actual.enableAutoPurge
        set(value) {
            actual.enableAutoPurge = value
        }

    @Suppress("UNCHECKED_CAST")
    public actual var headers: Map<String, String>?
        get() = actual.headers as Map<String, String>?
        set(value) {
            actual.headers = value as Map<Any?, *>?
        }

    public actual var isAcceptParentDomainCookies: Boolean
        get() = actual.acceptParentDomainCookies
        set(value) {
            actual.acceptParentDomainCookies = value
        }

    public actual var authenticator: Authenticator? = authenticator
        set(value) {
            field = value
            actual.authenticator = value?.actual
        }

    public actual var pinnedServerCertificate: ByteArray?
        get() = actual.pinnedServerCertificate?.toByteArray()
        set(value) {
            actual.pinnedServerCertificate = value?.toSecCertificate()
        }

    public actual var maxAttempts: Int
        get() = actual.maxAttempts.toInt()
        set(value) {
            if (value < 0) throw IllegalArgumentException("max attempts must be >=0")
            actual.maxAttempts = value.convert()
        }

    public actual var maxAttemptWaitTime: Int
        get() = actual.maxAttemptWaitTime.toInt()
        set(value) {
            if (value < 0) throw IllegalArgumentException("max attempt wait time must be >=0")
            actual.maxAttemptWaitTime = value.toDouble()
        }

    public actual var heartbeat: Int
        get() = actual.heartbeat.toInt()
        set(value) {
            if (value < 0) throw IllegalArgumentException("heartbeat must be >=0")
            val millis = value * 1000L
            require(millis <= Int.MAX_VALUE) { "heartbeat too large" }
            actual.heartbeat = value.toDouble()
        }

    @Deprecated("Use CollectionConfiguration.collections")
    public actual val database: Database
        get() {
            return collectionConfigurations.keys.firstOrNull()?.database
                ?: db
                ?: throw IllegalStateException("No database or collections provided for replication configuration")
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

    public override fun toString(): String {
        return buildString {
            append("ReplicatorConfig{(")
            collectionConfigurations.keys.forEachIndexed { i, c ->
                if (i != 0) append(", ")
                append(c.fullName)
            }
            append(") ")

            when (type) {
                ReplicatorType.PULL, ReplicatorType.PUSH_AND_PULL -> append('<')
                else -> {}
            }

            append(if (isContinuous) '*' else 'o')

            when (type) {
                ReplicatorType.PUSH, ReplicatorType.PUSH_AND_PULL -> append('>')
                else -> {}
            }

            if (authenticator != null) append('@')
            if (pinnedServerCertificate != null) append('^')

            append(target).append('}')
        }
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
public fun ReplicatorConfiguration.setPinnedServerSecCertificate(
    pinnedCert: SecCertificateRef?
): ReplicatorConfiguration {
    actual.pinnedServerCertificate = pinnedCert
    return this
}

/**
 * The remote target’s SSL certificate.
 */
public var ReplicatorConfiguration.pinnedServerSecCertificate: SecCertificateRef?
    get() = actual.pinnedServerCertificate
    set(value) {
        actual.pinnedServerCertificate = value
    }
