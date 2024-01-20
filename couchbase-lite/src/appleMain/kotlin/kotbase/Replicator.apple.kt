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

import cocoapods.CouchbaseLite.CBLReplicator
import kotbase.ext.asDispatchQueue
import kotbase.ext.toByteArray
import kotbase.ext.wrapCBLError
import kotbase.internal.DelegatedClass
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalStdlibApi::class)
public actual class Replicator
internal constructor(
    actual: CBLReplicator,
    private val _config: ReplicatorConfiguration
) : DelegatedClass<CBLReplicator>(actual), AutoCloseable {

    public actual constructor(config: ReplicatorConfiguration) : this(
        if (config.collections.isEmpty()) throw IllegalArgumentException("Attempt to configure a replicator with no source collections")
        else CBLReplicator(config.actual),
        config
    )

    internal actual constructor(config: ReplicatorConfiguration, test: Boolean) : this(config)

    @Suppress("DEPRECATION")
    public actual fun start() {
        config.database.mustBeOpen()
        actual.start()
    }

    public actual fun start(resetCheckpoint: Boolean) {
        actual.startWithReset(resetCheckpoint)
    }

    public actual fun stop() {
        actual.stop()
    }

    public actual val config: ReplicatorConfiguration
        get() = ReplicatorConfiguration(_config)

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(actual.status)

    public actual val serverCertificates: List<ByteArray>?
        get() = actual.serverCertificate?.toByteArray()?.let { listOf(it) }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Deprecated(
        "Use getPendingDocumentIds(Collection)",
        ReplaceWith("getPendingDocumentIds(config.database.defaultCollection)")
    )
    @Suppress("DEPRECATION", "ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val pendingDocumentIds: Set<String>
        get() {
            config.database.mustBeOpen()
            return wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                actual.pendingDocumentIDs(error) as Set<String>
            }
        }

    /**
     * Get a best effort set of document IDs in the default collection, that are still pending replication.
     */
    // For Objective-C/Swift throws
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use getPendingDocumentIds(Collection)",
        ReplaceWith("getPendingDocumentIds(config.database.defaultCollection())")
    )
    @Throws(CouchbaseLiteException::class)
    public fun pendingDocumentIds(): Set<String> = pendingDocumentIds

    @Suppress("DEPRECATION")
    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(collection: Collection): Set<String> {
        config.database.mustBeOpen()
        return wrapCBLError { error ->
            @Suppress("UNCHECKED_CAST")
            actual.pendingDocumentIDsForCollection(collection.actual, error) as Set<String>
        }
    }

    @Deprecated(
        "Use isDocumentPending(String, Collection)",
        ReplaceWith("isDocumentPending(docId, config.database.defaultCollection)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean {
        return wrapCBLError { error ->
            actual.isDocumentPending(docId, error)
        }
    }

    @Suppress("DEPRECATION")
    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String, collection: Collection): Boolean {
        config.database.mustBeOpen()
        return wrapCBLError { error ->
            actual.isDocumentPending(docId, collection.actual, error)
        }
    }

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        return ReplicatorListenerToken(
            actual.addChangeListener(listener.convert(this)),
            actual
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: ReplicatorChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListenerWithQueue(
            context[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        return SuspendReplicatorListenerToken(scope, token, actual)
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(scope: CoroutineScope, listener: ReplicatorChangeSuspendListener) {
        val token = actual.addChangeListenerWithQueue(
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        return ReplicatorListenerToken(
            actual.addDocumentReplicationListener(listener.convert(this)),
            actual
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addDocumentReplicationListener(
        context: CoroutineContext,
        listener: DocumentReplicationSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addDocumentReplicationListenerWithQueue(
            context[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        return SuspendReplicatorListenerToken(scope, token, actual)
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addDocumentReplicationListener(scope: CoroutineScope, listener: DocumentReplicationSuspendListener) {
        val token = actual.addDocumentReplicationListenerWithQueue(
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    @Deprecated(
        "Use ListenerToken.remove",
        ReplaceWith("token.remove()")
    )
    public actual fun removeChangeListener(token: ListenerToken) {
        token.remove()
    }

    actual override fun close() {
        // no close() in Objective-C SDK
        // https://github.com/couchbase/couchbase-lite-ios/blob/b1eca5996b06564e65ae1c0a1a8bb55db28f37f5/Objective-C/CBLReplicator.mm#L122
    }
}
