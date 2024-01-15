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

import kotbase.internal.fleece.toFLArray
import kotbase.internal.fleece.toFLDict
import kotbase.internal.fleece.toKString
import kotbase.util.to
import kotlinx.cinterop.*
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class ReplicatorConfiguration
private constructor(
    public actual val target: Endpoint,
    private val db: Database?,
    internal val collectionConfigurations: MutableMap<Collection, CollectionConfiguration> = mutableMapOf()
) {

    @Deprecated(
        "Use ReplicatorConfiguration(Endpoint)",
        ReplaceWith("ReplicatorConfiguration(target)")
    )
    public actual constructor(database: Database, target: Endpoint) : this(target, database) {
        addCollection(database.getDefaultCollection(), null)
    }

    public actual constructor(target: Endpoint) : this(target, null)

    public actual constructor(config: ReplicatorConfiguration) : this(
        config.target,
        config.db,
        config.collectionConfigurations.toMutableMap()
    ) {
        authenticator = config.authenticator
        isContinuous = config.isContinuous
        headers = config.headers
        isAcceptParentDomainCookies = config.isAcceptParentDomainCookies
        pinnedServerCertificate = config.pinnedServerCertificate
        type = config.type
        maxAttempts = config.maxAttempts
        maxAttemptWaitTime = config.maxAttemptWaitTime
        heartbeat = config.heartbeat
        isAutoPurgeEnabled = config.isAutoPurgeEnabled
    }

    internal constructor(config: ImmutableReplicatorConfiguration) : this(
        config.target,
        config.database,
        config.collectionConfigurations.toMutableMap()
    ) {
        authenticator = config.authenticator
        isContinuous = config.isContinuous
        headers = config.headers
        isAcceptParentDomainCookies = config.isAcceptParentDomainCookies
        pinnedServerCertificate = config.pinnedServerCertificate
        type = config.type
        maxAttempts = config.maxAttempts
        maxAttemptWaitTime = config.maxAttemptWaitTime
        heartbeat = config.heartbeat
        isAutoPurgeEnabled = config.isAutoPurgeEnabled
    }

    private fun checkCollection(collection: Collection) {
        val database = collectionConfigurations.keys.firstOrNull()?.database ?: db ?: collection.database
        if (database != collection.database) {
            throw IllegalArgumentException("Cannot add collection $collection because it does not belong to database ${database.name}.")
        }
        if (database.isClosed) {
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
        collectionConfigurations.remove(collection)
        return this
    }

    public actual fun setType(type: ReplicatorType): ReplicatorConfiguration {
        this.type = type
        return this
    }

    public actual fun setContinuous(continuous: Boolean): ReplicatorConfiguration {
        this.isContinuous = continuous
        return this
    }

    public actual fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration {
        this.isAutoPurgeEnabled = enabled
        return this
    }

    public actual fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration {
        this.headers = headers
        return this
    }

    public actual fun setAcceptParentDomainCookies(acceptParentCookies: Boolean): ReplicatorConfiguration {
        this@ReplicatorConfiguration.isAcceptParentDomainCookies = acceptParentCookies
        return this
    }

    public actual fun setAuthenticator(authenticator: Authenticator?): ReplicatorConfiguration {
        this.authenticator = authenticator
        return this
    }

    public actual fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration {
        this.pinnedServerCertificate = pinnedCert
        return this
    }

    public actual fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration {
        this.maxAttempts = maxAttempts
        return this
    }

    public actual fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration {
        this.maxAttemptWaitTime = maxAttemptWaitTime
        return this
    }

    public actual fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration {
        this.heartbeat = heartbeat
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setDocumentIDs")
    public actual fun setDocumentIDs(documentIDs: List<String>?): ReplicatorConfiguration {
        this.documentIDs = documentIDs
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setChannels")
    public actual fun setChannels(channels: List<String>?): ReplicatorConfiguration {
        this.channels = channels
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setConflictResolver")
    public actual fun setConflictResolver(conflictResolver: ConflictResolver?): ReplicatorConfiguration {
        this.conflictResolver = conflictResolver
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setPullFilter")
    public actual fun setPullFilter(pullFilter: ReplicationFilter?): ReplicatorConfiguration {
        this.pullFilter = pullFilter
        return this
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use CollectionConfiguration.setPushFilter")
    public actual fun setPushFilter(pushFilter: ReplicationFilter?): ReplicatorConfiguration {
        this.pushFilter = pushFilter
        return this
    }

    public actual fun getCollectionConfiguration(collection: Collection): CollectionConfiguration? =
        collectionConfigurations[collection]?.let(::CollectionConfiguration)

    public actual val collections: Set<Collection>
        get() = collectionConfigurations.keys

    public actual var type: ReplicatorType = Defaults.Replicator.TYPE

    public actual var isContinuous: Boolean = Defaults.Replicator.CONTINUOUS

    public actual var isAutoPurgeEnabled: Boolean = Defaults.Replicator.ENABLE_AUTO_PURGE

    public actual var headers: Map<String, String>? = null

    public actual var isAcceptParentDomainCookies: Boolean = Defaults.Replicator.ACCEPT_PARENT_COOKIES

    public actual var authenticator: Authenticator? = null

    public actual var pinnedServerCertificate: ByteArray? = null

    public actual var maxAttempts: Int = Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT
        set(value) {
            if (value < 0) throw IllegalArgumentException("max attempts must be >=0")
            field = value
        }

    public actual var maxAttemptWaitTime: Int = Defaults.Replicator.MAX_ATTEMPT_WAIT_TIME
        set(value) {
            if (value < 0) throw IllegalArgumentException("max attempt wait time must be >=0")
            field = value
        }

    public actual var heartbeat: Int = Defaults.Replicator.HEARTBEAT
        set(value) {
            if (value < 0) throw IllegalArgumentException("heartbeat must be >=0")
            val millis = value * 1000L
            require(millis <= Int.MAX_VALUE) { "heartbeat too large" }
            field = value
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

    private fun getDefaultCollectionConfiguration(): CollectionConfiguration =
        collectionConfigurations[defaultCollection]
            ?: throw IllegalArgumentException(
                "Cannot use legacy parameters when the default collection has no configuration"
            )

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

internal class ImmutableReplicatorConfiguration(config: ReplicatorConfiguration) {

    private val memory = object {
        val arena = Arena()
        val arrays = mutableListOf<FLArray>()
        val dicts = mutableListOf<FLDict>()
        val ref = StableRef.create(this@ImmutableReplicatorConfiguration)
    }

    private fun FLArray.retain(): FLArray {
        memory.arrays.add(this)
        return this
    }

    private fun FLDict.retain(): FLDict {
        memory.dicts.add(this)
        return this
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        with(it) {
            arena.clear()
            arrays.forEach { array ->
                FLArray_Release(array)
            }
            dicts.forEach { dict ->
                FLDict_Release(dict)
            }
            ref.dispose()
        }
    }

    @Suppress("DEPRECATION")
    val database: Database = config.database
    val collectionConfigurations: Map<Collection, CollectionConfiguration> = config.collectionConfigurations.toMap()
    val target: Endpoint = config.target
    val authenticator: Authenticator? = config.authenticator
    val isContinuous: Boolean = config.isContinuous
    val headers: Map<String, String>? = config.headers
    val isAcceptParentDomainCookies: Boolean = config.isAcceptParentDomainCookies
    val pinnedServerCertificate: ByteArray? = config.pinnedServerCertificate
    val type: ReplicatorType = config.type
    val maxAttempts: Int = config.maxAttempts
    val maxAttemptWaitTime: Int = config.maxAttemptWaitTime
    val heartbeat: Int = config.heartbeat
    val isAutoPurgeEnabled: Boolean = config.isAutoPurgeEnabled

    val actual: CPointer<CBLReplicatorConfiguration> =
        memory.arena.alloc<CBLReplicatorConfiguration>().also {
            it.acceptParentDomainCookies = config.isAcceptParentDomainCookies
            it.authenticator = config.authenticator?.actual
            val collectionsSize = config.collections.size
            it.collectionCount = collectionsSize.convert()
            it.collections = memory.arena.allocArray<CBLReplicationCollection>(collectionsSize).also { collections ->
                config.collections.forEachIndexed { index, collection ->
                    val collectionConfig = config.getCollectionConfiguration(collection) ?: return@forEachIndexed
                    collections[index].apply {
                        this.channels = collectionConfig.channels?.toFLArray()?.retain()
                        this.collection = collection.actual
                        this.conflictResolver = collectionConfig.nativeConflictResolver()
                        this.documentIDs = collectionConfig.documentIDs?.toFLArray()?.retain()
                        this.pullFilter = collectionConfig.nativePullFilter()
                        this.pushFilter = collectionConfig.nativePushFilter()
                    }
                }
            }
            it.context = memory.ref.asCPointer()
            it.continuous = config.isContinuous
            it.disableAutoPurge = !config.isAutoPurgeEnabled
            it.endpoint = config.target.actual
            it.headers = config.headers?.toFLDict()?.retain()
            it.heartbeat = config.heartbeat.convert()
            it.maxAttemptWaitTime = config.maxAttemptWaitTime.convert()
            it.maxAttempts = config.maxAttempts.convert()
            it.pinnedServerCertificate.apply {
                config.pinnedServerCertificate?.let { bytes ->
                    buf = memory.arena.allocArrayOf(bytes)
                    size = bytes.size.convert()
                }
            }
            it.proxy = null
            it.replicatorType = config.type.actual
        }.ptr
}

private fun CollectionConfiguration.nativeConflictResolver(): CBLConflictResolver? {
    if (conflictResolver == null) return null
    return staticCFunction { ref, documentId, localDoc, remoteDoc ->
        val config = ref.to<ImmutableReplicatorConfiguration>()
        val localDocument = localDoc?.asDocument(config.database)
        val remoteDocument = remoteDoc?.asDocument(config.database)
        val collection = localDocument?.collection
            ?: remoteDocument?.collection
            ?: return@staticCFunction null
        val collectionConfig = config.collectionConfigurations[collection]
            ?: return@staticCFunction null
        collectionConfig.conflictResolver!!.invoke(
            Conflict(
                documentId.toKString()!!,
                localDocument,
                remoteDocument
            )
        )?.actual
    }
}

private fun CollectionConfiguration.nativePullFilter(): CBLReplicationFilter? {
    if (pullFilter == null) return null
    return staticCFunction { ref, doc, flags ->
        val config = ref.to<ImmutableReplicatorConfiguration>()
        val document = Document(doc!!, config.database)
        val collection = document.collection
            ?: return@staticCFunction true
        val collectionConfig = config.collectionConfigurations[collection]
            ?: return@staticCFunction true
        collectionConfig.pullFilter!!.invoke(
            document,
            flags.toDocumentFlags()
        )
    }
}

private fun CollectionConfiguration.nativePushFilter(): CBLReplicationFilter? {
    if (pushFilter == null) return null
    return staticCFunction { ref, doc, flags ->
        val config = ref.to<ImmutableReplicatorConfiguration>()
        val document = Document(doc!!, config.database)
        val collection = document.collection
            ?: return@staticCFunction true
        val collectionConfig = config.collectionConfigurations[collection]
            ?: return@staticCFunction true
        collectionConfig.pushFilter!!.invoke(
            document,
            flags.toDocumentFlags()
        )
    }
}
