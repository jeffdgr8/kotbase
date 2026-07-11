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
    internal val collectionConfigurations: MutableMap<Collection, CollectionConfiguration>
) {

    public actual constructor(
        collections: kotlin.collections.Collection<CollectionConfiguration>,
        target: Endpoint
    ) : this(
        target,
        collections.validated().associateByTo(mutableMapOf()) { it.collection }
    )

    public actual constructor(config: ReplicatorConfiguration) : this(
        config.target,
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

    public actual val collections: Set<CollectionConfiguration>
        get() = collectionConfigurations.values.toSet()

    public actual var type: ReplicatorType = Defaults.Replicator.TYPE

    public actual var isContinuous: Boolean = Defaults.Replicator.CONTINUOUS

    public actual var isAutoPurgeEnabled: Boolean = Defaults.Replicator.ENABLE_AUTO_PURGE

    public actual var headers: Map<String, String>? = null

    public actual var isAcceptParentDomainCookies: Boolean = Defaults.Replicator.ACCEPT_PARENT_COOKIES

    public actual var authenticator: Authenticator? = null

    public actual var pinnedServerCertificate: ByteArray? = null

    public actual var maxAttempts: Int = Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT
        set(value) {
            require(value >= 0) { "max attempts must be >=0" }
            field = value
        }

    public actual var maxAttemptWaitTime: Int = Defaults.Replicator.MAX_ATTEMPTS_WAIT_TIME
        set(value) {
            require(value >= 0) { "max attempt wait time must be >=0" }
            field = value
        }

    public actual var heartbeat: Int = Defaults.Replicator.HEARTBEAT
        set(value) {
            require(value >= 0) { "heartbeat must be >=0" }
            val millis = value * 1000L
            require(millis <= Int.MAX_VALUE) { "heartbeat too large" }
            field = value
        }

    override fun toString(): String {
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

    val collectionConfigurations: Map<Collection, CollectionConfiguration> = config.collectionConfigurations.toMap()
    val database: Database
        get() = collectionConfigurations.keys.first().database
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
            val collectionConfigs = config.collections.toList()
            it.collectionCount = collectionConfigs.size.convert()
            it.collections = memory.arena.allocArray<CBLReplicationCollection>(collectionConfigs.size).also { collections ->
                collectionConfigs.forEachIndexed { index, collectionConfig ->
                    collections[index].apply {
                        this.channels = collectionConfig.channels?.toFLArray()?.retain()
                        this.collection = collectionConfig.collection.actual
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
        val localDocument = localDoc?.asDocument(config.database, release = false)
        val remoteDocument = remoteDoc?.asDocument(config.database, release = false)
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
        val document = Document(doc!!, config.database, release = false)
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
        val document = Document(doc!!, config.database, release = false)
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

public actual fun ReplicatorConfiguration.setAllowReplicatingInBackground(allowReplicatingInBackground: Boolean): ReplicatorConfiguration {
    // no-op
    return this
}

public actual var ReplicatorConfiguration.allowReplicatingInBackground: Boolean
    get() = Defaults.Replicator.ALLOW_REPLICATING_IN_BACKGROUND
    set(_) {
        // no-op
    }

private fun kotlin.collections.Collection<CollectionConfiguration>.validated(): kotlin.collections.Collection<CollectionConfiguration> {
    val db = firstOrNull()?.collection?.database
    forEach { config ->
        require(config.collection.database == db) {
            "Cannot create a configuration for collections from different databases"
        }
    }
    return this
}
