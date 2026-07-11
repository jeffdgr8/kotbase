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

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * **ENTERPRISE EDITION API**
 *
 * The MultipeerReplicator replicates database collections between devices in a
 * multipeer mesh network. It provides peer discovery, connection management,
 * and data replication.
 *
 * @constructor Initializes the MultipeerReplicator with the given configuration.
 *
 * @param config The configuration for the replicator.
 * @throws CouchbaseLiteException on error creating the replicator.
 */
public expect class MultipeerReplicator
@Throws(CouchbaseLiteException::class)
public constructor(config: MultipeerReplicatorConfiguration) : AutoCloseable {

    /**
     * The configuration.
     */
    public val config: MultipeerReplicatorConfiguration

    /**
     * The peer ID of this replicator.
     */
    public val peerId: PeerInfo.PeerId

    /**
     * The set of peer IDs currently online; empty if none.
     *
     * Warning: This property can block for several seconds in some cases. It is recommended
     * to not access it on the UI thread. This is a known issue we plan to address in a
     * future release.
     */
    public val neighborPeers: Set<PeerInfo.PeerId>

    /**
     * Returns the current status of this replicator for the given transport, or null
     * if the transport is not configured.
     *
     * Warning: This method can block for several seconds in some cases. It is recommended to not
     * call this on the UI thread. This is a known issue we plan to address in a future release.
     *
     * @param transport The transport whose status is being queried.
     * @return The status for the given transport, or null if the transport
     *     is not configured for this replicator.
     */
    public fun getStatus(transport: MultipeerTransport): MultipeerReplicatorStatus?

    /**
     * Starts peer discovery and initiates replication with connected peers.
     */
    public fun start()

    /**
     * Stops peer discovery and all replicators.
     */
    public fun stop()

    /**
     * Returns information about the peer with the given ID, or null if no such peer
     * is known.
     *
     * Warning: This method can block for several seconds in some cases. It is recommended to not
     * call this on the UI thread. This is a known issue we plan to address in a future release.
     *
     * @param peerId The ID of the peer.
     * @return The peer's info, or null if no peer with that ID is known.
     */
    public fun getPeerInfo(peerId: PeerInfo.PeerId): PeerInfo?

    /**
     * Adds a listener to receive updates on the multipeer replicator status.
     *
     * The updates will be delivered on the main thread.
     *
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addStatusListener(listener: MultipeerReplicatorStatusListener): ListenerToken

    /**
     * Adds a listener to receive updates on the multipeer replicator status with a [CoroutineContext]
     * that will be used to launch coroutines the listener will be called on. Coroutines will be launched in
     * a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addStatusListener(
        context: CoroutineContext,
        listener: MultipeerReplicatorStatusSuspendListener
    ): ListenerToken

    /**
     * Adds a listener to receive updates on the multipeer replicator status with a [CoroutineScope]
     * that will be used to launch coroutines the listener will be called on. The listener is removed when
     * the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to be added.
     */
    public fun addStatusListener(scope: CoroutineScope, listener: MultipeerReplicatorStatusSuspendListener)

    /**
     * Adds a listener to receive updates on the peer discovery status.
     *
     * The updates will be delivered on the main thread.
     *
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addPeerDiscoveryStatusListener(listener: PeerDiscoveryStatusListener): ListenerToken

    /**
     * Adds a listener to receive updates on the peer discovery status with a [CoroutineContext]
     * that will be used to launch coroutines the listener will be called on. Coroutines will be launched in
     * a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addPeerDiscoveryStatusListener(
        context: CoroutineContext,
        listener: PeerDiscoveryStatusSuspendListener
    ): ListenerToken

    /**
     * Adds a listener to receive updates on the peer discovery status with a [CoroutineScope]
     * that will be used to launch coroutines the listener will be called on. The listener is removed when
     * the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to be added.
     */
    public fun addPeerDiscoveryStatusListener(scope: CoroutineScope, listener: PeerDiscoveryStatusSuspendListener)

    /**
     * Adds a listener to receive updates on the replicator status for each connected peer.
     *
     * The updates will be delivered on the main thread.
     *
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addPeerReplicatorStatusListener(listener: PeerReplicatorStatusListener): ListenerToken

    /**
     * Adds a listener to receive updates on the replicator status for each connected peer with a
     * [CoroutineContext] that will be used to launch coroutines the listener will be called on.
     * Coroutines will be launched in a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addPeerReplicatorStatusListener(
        context: CoroutineContext,
        listener: PeerReplicatorStatusSuspendListener
    ): ListenerToken

    /**
     * Adds a listener to receive updates on the replicator status for each connected peer with a
     * [CoroutineScope] that will be used to launch coroutines the listener will be called on.
     * The listener is removed when the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to be added.
     */
    public fun addPeerReplicatorStatusListener(scope: CoroutineScope, listener: PeerReplicatorStatusSuspendListener)

    /**
     * Adds a listener to receive updates on the document replication status for each connected peer.
     *
     * The updates will be delivered on the main thread.
     *
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addPeerDocumentReplicationListener(listener: PeerDocumentReplicationListener): ListenerToken

    /**
     * Adds a listener to receive updates on the document replication status for each connected peer with a
     * [CoroutineContext] that will be used to launch coroutines the listener will be called on.
     * Coroutines will be launched in a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to be added.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addPeerDocumentReplicationListener(
        context: CoroutineContext,
        listener: PeerDocumentReplicationSuspendListener
    ): ListenerToken

    /**
     * Adds a listener to receive updates on the document replication status for each connected peer with a
     * [CoroutineScope] that will be used to launch coroutines the listener will be called on.
     * The listener is removed when the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to be added.
     */
    public fun addPeerDocumentReplicationListener(scope: CoroutineScope, listener: PeerDocumentReplicationSuspendListener)

    /**
     * Immediately release the native replicator and all its resources. If the replicator is
     * still running, this method makes a best-effort attempt to stop it.
     */
    override fun close()
}
