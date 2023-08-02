package kotbase

import kotbase.internal.fleece.toFLArray
import kotbase.internal.fleece.toFLDict
import kotbase.internal.fleece.toKString
import kotbase.util.to
import kotlinx.cinterop.*
import libcblite.*
import kotlin.native.internal.createCleaner

public actual class ReplicatorConfiguration actual constructor(
    public actual val database: Database,
    public actual val target: Endpoint
) {

    public actual constructor(config: ReplicatorConfiguration) : this(
        config.database,
        config.target
    ) {
        authenticator = config.authenticator
        channels = config.channels
        conflictResolver = config.conflictResolver
        isContinuous = config.isContinuous
        documentIDs = config.documentIDs
        headers = config.headers
        pinnedServerCertificate = config.pinnedServerCertificate
        pullFilter = config.pullFilter
        pushFilter = config.pushFilter
        type = config.type
        maxAttempts = config.maxAttempts
        maxAttemptWaitTime = config.maxAttemptWaitTime
        heartbeat = config.heartbeat
        isAutoPurgeEnabled = config.isAutoPurgeEnabled
    }

    internal constructor(config: ImmutableReplicatorConfiguration) : this(
        config.database,
        config.target
    ) {
        authenticator = config.authenticator
        channels = config.channels
        conflictResolver = config.conflictResolver
        isContinuous = config.isContinuous
        documentIDs = config.documentIDs
        headers = config.headers
        pinnedServerCertificate = config.pinnedServerCertificate
        pullFilter = config.pullFilter
        pushFilter = config.pushFilter
        type = config.type
        maxAttempts = config.maxAttempts
        maxAttemptWaitTime = config.maxAttemptWaitTime
        heartbeat = config.heartbeat
        isAutoPurgeEnabled = config.isAutoPurgeEnabled
    }

    public actual fun setAuthenticator(authenticator: Authenticator): ReplicatorConfiguration {
        this.authenticator = authenticator
        return this
    }

    public actual fun setChannels(channels: List<String>?): ReplicatorConfiguration {
        this.channels = channels
        return this
    }

    public actual fun setConflictResolver(conflictResolver: ConflictResolver?): ReplicatorConfiguration {
        this.conflictResolver = conflictResolver
        return this
    }

    public actual fun setContinuous(continuous: Boolean): ReplicatorConfiguration {
        this.isContinuous = continuous
        return this
    }

    public actual fun setDocumentIDs(documentIDs: List<String>?): ReplicatorConfiguration {
        this.documentIDs = documentIDs
        return this
    }

    public actual fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration {
        this.headers = headers
        return this
    }

    public actual fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration {
        this.pinnedServerCertificate = pinnedCert
        return this
    }

    public actual fun setPullFilter(pullFilter: ReplicationFilter?): ReplicatorConfiguration {
        this.pullFilter = pullFilter
        return this
    }

    public actual fun setPushFilter(pushFilter: ReplicationFilter?): ReplicatorConfiguration {
        this.pushFilter = pushFilter
        return this
    }

    public actual fun setType(type: ReplicatorType): ReplicatorConfiguration {
        this.type = type
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

    public actual fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration {
        this.isAutoPurgeEnabled = enabled
        return this
    }

    public actual var authenticator: Authenticator? = null

    public actual var channels: List<String>? = null

    public actual var conflictResolver: ConflictResolver? = null

    public actual var isContinuous: Boolean = false

    public actual var documentIDs: List<String>? = null

    public actual var headers: Map<String, String>? = null

    public actual var pinnedServerCertificate: ByteArray? = null

    public actual var pullFilter: ReplicationFilter? = null

    public actual var pushFilter: ReplicationFilter? = null

    public actual var type: ReplicatorType = ReplicatorType.PUSH_AND_PULL

    public actual var maxAttempts: Int = 0

    public actual var maxAttemptWaitTime: Int = 0

    public actual var heartbeat: Int = 0

    public actual var isAutoPurgeEnabled: Boolean = true

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

    val database: Database = config.database
    val target: Endpoint = config.target
    val authenticator: Authenticator? = config.authenticator
    val channels: List<String>? = config.channels
    val conflictResolver: ConflictResolver? = config.conflictResolver
    val isContinuous: Boolean = config.isContinuous
    val documentIDs: List<String>? = config.documentIDs
    val headers: Map<String, String>? = config.headers
    val pinnedServerCertificate: ByteArray? = config.pinnedServerCertificate
    val pullFilter: ReplicationFilter? = config.pullFilter
    val pushFilter: ReplicationFilter? = config.pushFilter
    val type: ReplicatorType = config.type
    val maxAttempts: Int = config.maxAttempts
    val maxAttemptWaitTime: Int = config.maxAttemptWaitTime
    val heartbeat: Int = config.heartbeat
    val isAutoPurgeEnabled: Boolean = config.isAutoPurgeEnabled

    val actual: CPointer<CBLReplicatorConfiguration> =
        memory.arena.alloc<CBLReplicatorConfiguration>().also {
            it.authenticator = config.authenticator?.actual
            it.channels = config.channels?.toFLArray()?.retain()
            it.conflictResolver = nativeConflictResolver()
            it.context = memory.ref.asCPointer()
            it.continuous = config.isContinuous
            it.database = config.database.actual
            it.disableAutoPurge = !config.isAutoPurgeEnabled
            it.documentIDs = config.documentIDs?.toFLArray()?.retain()
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
            it.pullFilter = nativePullFilter()
            it.pushFilter = nativePushFilter()
            it.replicatorType = config.type.actual
        }.ptr

    private fun nativeConflictResolver(): CBLConflictResolver? {
        if (conflictResolver == null) return null
        return staticCFunction { ref, documentId, localDocument, remoteDocument ->
            val config = ref.to<ImmutableReplicatorConfiguration>()
            config.conflictResolver!!.invoke(
                Conflict(
                    documentId.toKString()!!,
                    localDocument?.asDocument(config.database),
                    remoteDocument?.asDocument(config.database)
                )
            )?.actual
        }
    }

    private fun nativePullFilter(): CBLReplicationFilter? {
        if (pullFilter == null) return null
        return staticCFunction { ref, document, flags ->
            val config = ref.to<ImmutableReplicatorConfiguration>()
            config.pullFilter!!.invoke(
                Document(document!!, config.database),
                flags.toDocumentFlags()
            )
        }
    }

    private fun nativePushFilter(): CBLReplicationFilter? {
        if (pushFilter == null) return null
        return staticCFunction { ref, document, flags ->
            val config = ref.to<ImmutableReplicatorConfiguration>()
            config.pushFilter!!.invoke(
                Document(document!!, config.database),
                flags.toDocumentFlags()
            )
        }
    }
}
