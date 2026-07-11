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

import cocoapods.CouchbaseLite.CBLMultipeerReplicator
import cocoapods.CouchbaseLite.CBLPeerID
import kotbase.ext.asDispatchQueue
import kotbase.ext.dispatcher
import kotbase.ext.wrapCBLError
import kotbase.internal.DelegatedClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

public actual class MultipeerReplicator
internal constructor(
    actual: CBLMultipeerReplicator,
    private val _config: MultipeerReplicatorConfiguration
) : DelegatedClass<CBLMultipeerReplicator>(actual), AutoCloseable {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(config: MultipeerReplicatorConfiguration) : this(
        wrapCBLError { error ->
            CBLMultipeerReplicator(config.actual, error)
        },
        config
    )

    public actual val config: MultipeerReplicatorConfiguration
        get() = MultipeerReplicatorConfiguration(_config)

    public actual val peerId: PeerInfo.PeerId
        get() = PeerInfo.PeerId(actual.peerID)

    public actual val neighborPeers: Set<PeerInfo.PeerId>
        get() = actual.neighborPeers.map {
            @Suppress("UNCHECKED_CAST")
            PeerInfo.PeerId(it as CBLPeerID)
        }.toSet()

    public actual fun getStatus(transport: MultipeerTransport): MultipeerReplicatorStatus? =
        actual.statusForTransport(transport.actual)?.let { MultipeerReplicatorStatus(it) }

    public actual fun start() {
        actual.start()
    }

    public actual fun stop() {
        actual.stop()
    }

    public actual fun getPeerInfo(peerId: PeerInfo.PeerId): PeerInfo? =
        actual.peerInfoForPeerID(peerId.actual)?.let { PeerInfo(it) }

    public actual fun addStatusListener(listener: MultipeerReplicatorStatusListener): ListenerToken =
        DelegatedListenerToken(
            actual.addStatusListenerWithQueue(null, listener.convert())
        )

    public actual fun addStatusListener(
        context: CoroutineContext,
        listener: MultipeerReplicatorStatusSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addStatusListenerWithQueue(
            context.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addStatusListener(scope: CoroutineScope, listener: MultipeerReplicatorStatusSuspendListener) {
        val token = actual.addStatusListenerWithQueue(
            scope.coroutineContext.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addPeerDiscoveryStatusListener(listener: PeerDiscoveryStatusListener): ListenerToken =
        DelegatedListenerToken(
            actual.addPeerDiscoveryStatusListenerWithQueue(null, listener.convert())
        )

    public actual fun addPeerDiscoveryStatusListener(
        context: CoroutineContext,
        listener: PeerDiscoveryStatusSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addPeerDiscoveryStatusListenerWithQueue(
            context.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addPeerDiscoveryStatusListener(
        scope: CoroutineScope,
        listener: PeerDiscoveryStatusSuspendListener
    ) {
        val token = actual.addPeerDiscoveryStatusListenerWithQueue(
            scope.coroutineContext.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addPeerReplicatorStatusListener(listener: PeerReplicatorStatusListener): ListenerToken =
        DelegatedListenerToken(
            actual.addPeerReplicatorStatusListenerWithQueue(null, listener.convert())
        )

    public actual fun addPeerReplicatorStatusListener(
        context: CoroutineContext,
        listener: PeerReplicatorStatusSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addPeerReplicatorStatusListenerWithQueue(
            context.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addPeerReplicatorStatusListener(
        scope: CoroutineScope,
        listener: PeerReplicatorStatusSuspendListener
    ) {
        val token = actual.addPeerReplicatorStatusListenerWithQueue(
            scope.coroutineContext.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addPeerDocumentReplicationListener(listener: PeerDocumentReplicationListener): ListenerToken =
        DelegatedListenerToken(
            actual.addPeerDocumentReplicationListenerWithQueue(null, listener.convert())
        )

    public actual fun addPeerDocumentReplicationListener(
        context: CoroutineContext,
        listener: PeerDocumentReplicationSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addPeerDocumentReplicationListenerWithQueue(
            context.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addPeerDocumentReplicationListener(
        scope: CoroutineScope,
        listener: PeerDocumentReplicationSuspendListener
    ) {
        val token = actual.addPeerDocumentReplicationListenerWithQueue(
            scope.coroutineContext.dispatcher?.asDispatchQueue(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    actual override fun close() {
        // no close() in Objective-C SDK: make a best-effort attempt to stop
        stop()
    }
}
