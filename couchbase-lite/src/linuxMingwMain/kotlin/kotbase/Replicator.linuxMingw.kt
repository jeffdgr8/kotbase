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

import cnames.structs.CBLReplicator
import kotbase.internal.fleece.keys
import kotbase.internal.fleece.toFLString
import kotbase.internal.fleece.toKString
import kotbase.internal.wrapCBLError
import kotbase.util.to
import kotbase.util.toList
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import libcblite.*
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Replicator
private constructor(
    actual: CPointer<CBLReplicator>,
    private val immutableConfig: ImmutableReplicatorConfiguration
) : AutoCloseable {

    private val memory = object {
        var closeCalled = false
        val actual = actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled) {
            CBLReplicator_Release(it.actual)
        }
    }

    public actual constructor(config: ReplicatorConfiguration) : this(
        if (config.collections.isNotEmpty()) ImmutableReplicatorConfiguration(config)
        else throw IllegalArgumentException("Attempt to configure a replicator with no source collections")
    )

    private constructor(config: ImmutableReplicatorConfiguration) : this(
        wrapCBLError { error ->
            CBLReplicator_Create(config.actual, error)
        }!!,
        config
    )

    internal actual constructor(config: ReplicatorConfiguration, test: Boolean) : this(config)

    internal val actual: CPointer<CBLReplicator>
        get() = memory.actual

    public actual fun start() {
        sourceDatabase.mustBeOpen()
        CBLReplicator_Start(actual, false)
    }

    public actual fun start(resetCheckpoint: Boolean) {
        CBLReplicator_Start(actual, resetCheckpoint)
    }

    public actual fun stop() {
        CBLReplicator_Stop(actual)
    }

    public actual val config: ReplicatorConfiguration
        get() = ReplicatorConfiguration(immutableConfig)

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(CBLReplicator_Status(actual))

    public actual val correlationId: String?
        get() = CBLReplicator_CorrelationID(actual).toKString()

    private val sourceDatabase: Database
        get() = immutableConfig.collectionConfigurations.keys.first().database

    public actual val serverCertificates: List<ByteArray>?
        get() = null

    private fun checkPullOnlyPendingDocIds() {
        if (config.type == ReplicatorType.PULL) {
            throw CouchbaseLiteException(
                "Pending Document IDs are not supported on pull-only replicators.",
                CBLError.Domain.CBLITE,
                CBLError.Code.UNSUPPORTED
            )
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(collection: Collection): Set<String> {
        checkPullOnlyPendingDocIds()
        sourceDatabase.mustBeOpen()
        return wrapCBLError { error ->
            val dict = CBLReplicator_PendingDocumentIDs(actual, collection.actual, error)
            dict?.keys()?.also {
                FLDict_Release(dict)
            }?.toSet() ?: emptySet()
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String, collection: Collection): Boolean {
        sourceDatabase.mustBeOpen()
        return wrapCBLError { error ->
            memScoped {
                CBLReplicator_IsDocumentPending(actual, docId.toFLString(this), collection.actual, error)
            }
        }
    }

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        val holder = ReplicatorChangeDefaultListenerHolder(listener, this)
        return addNativeChangeListener(holder)
    }

    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: ReplicatorChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val holder = ReplicatorChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        return SuspendListenerToken(scope, token)
    }

    public actual fun addChangeListener(scope: CoroutineScope, listener: ReplicatorChangeSuspendListener) {
        val holder = ReplicatorChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    private fun addNativeChangeListener(holder: ReplicatorChangeListenerHolder) =
        StableRefListenerToken(holder) {
            CBLReplicator_AddChangeListener(actual, nativeChangeListener(), it)!!
        }

    private fun nativeChangeListener(): CBLReplicatorChangeListener {
        return staticCFunction { ref, _, status ->
            with(ref.to<ReplicatorChangeListenerHolder>()) {
                val change = ReplicatorChange(replicator, ReplicatorStatus(status!!))
                when (this) {
                    is ReplicatorChangeDefaultListenerHolder -> listener(change)
                    is ReplicatorChangeSuspendListenerHolder -> scope.launch {
                        listener(change)
                    }
                }
            }
        }
    }

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        val holder = DocumentReplicationDefaultListenerHolder(listener, this)
        return addNativeDocumentReplicationListener(holder)
    }

    public actual fun addDocumentReplicationListener(
        context: CoroutineContext,
        listener: DocumentReplicationSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val holder = DocumentReplicationSuspendListenerHolder(listener, this, scope)
        val token = addNativeDocumentReplicationListener(holder)
        return SuspendListenerToken(scope, token)
    }

    public actual fun addDocumentReplicationListener(
        scope: CoroutineScope,
        listener: DocumentReplicationSuspendListener
    ) {
        val holder = DocumentReplicationSuspendListenerHolder(listener, this, scope)
        val token = addNativeDocumentReplicationListener(holder)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    private fun addNativeDocumentReplicationListener(holder: DocumentReplicationListenerHolder) =
        StableRefListenerToken(holder) {
            CBLReplicator_AddDocumentReplicationListener(actual, nativeDocumentReplicationListener(), it)!!
        }

    private fun nativeDocumentReplicationListener(): CBLDocumentReplicationListener {
        return staticCFunction { ref, _, isPush, numDocuments, docs ->
            // ReplicatorEETest.testCollectionDefaultConflictResolver()
            // receives listener callbacks with no documents
            // Java and Objective-C SDKs don't do this
            if (numDocuments == 0U) return@staticCFunction
            val documents = docs!!.toList(numDocuments) { ReplicatedDocument(it) }
            with(ref.to<DocumentReplicationListenerHolder>()) {
                val replication = DocumentReplication(replicator, isPush, documents)
                when (this) {
                    is DocumentReplicationDefaultListenerHolder -> listener(replication)
                    is DocumentReplicationSuspendListenerHolder -> scope.launch {
                        listener(replication)
                    }
                }
            }
        }
    }

    actual override fun close() {
        memory.closeCalled = true
        stop()
        CBLReplicator_Release(actual)
    }

    public actual val isClosed: Boolean
        get() = memory.closeCalled
}
