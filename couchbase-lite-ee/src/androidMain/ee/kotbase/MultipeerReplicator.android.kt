/*
 * Copyright 2026 Jeff Lockhart
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

import android.content.Context
import kotbase.ext.dispatcher
import kotbase.internal.DelegatedClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlin.coroutines.CoroutineContext
import com.couchbase.lite.MultipeerReplicator as CBLMultipeerReplicator

public actual class MultipeerReplicator
internal constructor(
    actual: CBLMultipeerReplicator,
    private val _config: MultipeerReplicatorConfiguration
) : DelegatedClass<CBLMultipeerReplicator>(actual), AutoCloseable {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(config: MultipeerReplicatorConfiguration) : this(
        CBLMultipeerReplicator(config.actual),
        config
    )

    public actual val config: MultipeerReplicatorConfiguration
        get() = MultipeerReplicatorConfiguration(_config)

    public actual val peerId: PeerInfo.PeerId
        get() = PeerInfo.PeerId(actual.peerId)

    public actual val neighborPeers: Set<PeerInfo.PeerId>
        get() = actual.neighborPeers.map { PeerInfo.PeerId(it) }.toSet()

    public actual fun getStatus(transport: MultipeerTransport): MultipeerReplicatorStatus? =
        actual.getStatus(transport)?.let { MultipeerReplicatorStatus(it) }

    public actual fun start() {
        actual.start()
    }

    public actual fun stop() {
        actual.stop()
    }

    public actual fun getPeerInfo(peerId: PeerInfo.PeerId): PeerInfo? =
        actual.getPeerInfo(peerId.actual)?.let { PeerInfo(it) }

    /**
     * Returns the set of necessary permissions required by the application.
     *
     * @return A set of necessary permissions (Strings).
     */
    public fun getNecessaryPermissions(): Set<String> =
        actual.necessaryPermissions

    /**
     * Return the set of missing (not granted) permissions.
     *
     * @param context Application context
     */
    public fun getMissingPermissions(context: Context): Set<String> =
        actual.getMissingPermissions(context)

    public actual fun addStatusListener(listener: MultipeerReplicatorStatusListener): ListenerToken =
        DelegatedListenerToken(actual.addStatusListener(listener.convert()))

    public actual fun addStatusListener(
        context: CoroutineContext,
        listener: MultipeerReplicatorStatusSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addStatusListener(
            context.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addStatusListener(scope: CoroutineScope, listener: MultipeerReplicatorStatusSuspendListener) {
        val token = actual.addStatusListener(
            scope.coroutineContext.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addPeerDiscoveryStatusListener(listener: PeerDiscoveryStatusListener): ListenerToken =
        DelegatedListenerToken(actual.addPeerDiscoveryStatusListener(listener.convert()))

    public actual fun addPeerDiscoveryStatusListener(
        context: CoroutineContext,
        listener: PeerDiscoveryStatusSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addPeerDiscoveryStatusListener(
            context.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addPeerDiscoveryStatusListener(
        scope: CoroutineScope,
        listener: PeerDiscoveryStatusSuspendListener
    ) {
        val token = actual.addPeerDiscoveryStatusListener(
            scope.coroutineContext.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addPeerReplicatorStatusListener(listener: PeerReplicatorStatusListener): ListenerToken =
        DelegatedListenerToken(actual.addPeerReplicatorStatusListener(listener.convert()))

    public actual fun addPeerReplicatorStatusListener(
        context: CoroutineContext,
        listener: PeerReplicatorStatusSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addPeerReplicatorStatusListener(
            context.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addPeerReplicatorStatusListener(
        scope: CoroutineScope,
        listener: PeerReplicatorStatusSuspendListener
    ) {
        val token = actual.addPeerReplicatorStatusListener(
            scope.coroutineContext.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addPeerDocumentReplicationListener(listener: PeerDocumentReplicationListener): ListenerToken =
        DelegatedListenerToken(actual.addPeerDocumentReplicationListener(listener.convert()))

    public actual fun addPeerDocumentReplicationListener(
        context: CoroutineContext,
        listener: PeerDocumentReplicationSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addPeerDocumentReplicationListener(
            context.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addPeerDocumentReplicationListener(
        scope: CoroutineScope,
        listener: PeerDocumentReplicationSuspendListener
    ) {
        val token = actual.addPeerDocumentReplicationListener(
            scope.coroutineContext.dispatcher?.asExecutor(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    actual override fun close() {
        actual.close()
    }
}
