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
import kotbase.internal.DelegatedClass
import kotbase.ext.asDispatchQueue
import kotbase.ext.toByteArray
import kotbase.ext.wrapCBLError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

public actual class Replicator
internal constructor(
    actual: CBLReplicator,
    private val _config: ReplicatorConfiguration
) : DelegatedClass<CBLReplicator>(actual) {

    public actual constructor(config: ReplicatorConfiguration) : this(
        CBLReplicator(config.actual),
        config
    )

    internal actual constructor(config: ReplicatorConfiguration, test: Boolean) : this(config)

    public actual fun start() {
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

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(): Set<String> {
        return wrapCBLError { error ->
            @Suppress("UNCHECKED_CAST")
            actual.pendingDocumentIDs(error) as Set<String>
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean {
        return wrapCBLError { error ->
            actual.isDocumentPending(docId, error)
        }
    }

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addChangeListener(listener.convert(this))
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
        return SuspendListenerToken(scope, DelegatedListenerToken(token))
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(scope: CoroutineScope, listener: ReplicatorChangeSuspendListener) {
        val token = actual.addChangeListenerWithQueue(
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            actual.removeChangeListenerWithToken(token)
        }
    }

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addDocumentReplicationListener(listener.convert(this))
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
        return SuspendListenerToken(scope, DelegatedListenerToken(token))
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addDocumentReplicationListener(scope: CoroutineScope, listener: DocumentReplicationSuspendListener) {
        val token = actual.addDocumentReplicationListenerWithQueue(
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            actual.removeChangeListenerWithToken(token)
        }
    }

    public actual fun removeChangeListener(token: ListenerToken) {
        if (token is SuspendListenerToken) {
            actual.removeChangeListenerWithToken(token.token.actual)
            token.scope.cancel()
        } else {
            token as DelegatedListenerToken
            actual.removeChangeListenerWithToken(token.actual)
        }
    }
}