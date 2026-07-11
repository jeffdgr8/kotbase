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

import cocoapods.CouchbaseLite.CBLPeerID
import cocoapods.CouchbaseLite.CBLPeerInfo
import kotbase.ext.toByteArray
import kotbase.internal.DelegatedClass

public actual class PeerInfo
internal constructor(actual: CBLPeerInfo) : DelegatedClass<CBLPeerInfo>(actual) {

    public actual class PeerId
    internal constructor(actual: CBLPeerID) : DelegatedClass<CBLPeerID>(actual) {

        public actual val bytes: ByteArray
            get() = actual.bytes.toByteArray()
    }

    public actual val peerId: PeerId
        get() = PeerId(actual.peerID)

    public actual val certificate: ByteArray?
        get() = actual.certificate?.toByteArray()

    public actual val isOnline: Boolean
        get() = actual.online

    public actual val transports: Set<MultipeerTransport>
        get() = actual.transports.toMultipeerTransportSet()

    public actual val replicatorTransport: MultipeerTransport?
        get() = actual.replicatorTransport.toMultipeerTransportOrNull()

    public actual val replicatorStatus: ReplicatorStatus
        get() = ReplicatorStatus(actual.replicatorStatus)

    public actual val neighbors: Set<PeerId>
        get() = actual.neighborPeers.map {
            @Suppress("UNCHECKED_CAST")
            PeerId(it as CBLPeerID)
        }.toSet()

    public actual companion object {

        public actual val PEER_ID_LENGTH: Int = 32
    }
}
