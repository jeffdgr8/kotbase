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

import kotbase.internal.DelegatedClass
import com.couchbase.lite.PeerInfo as CBLPeerInfo

public actual class PeerInfo
internal constructor(actual: CBLPeerInfo) : DelegatedClass<CBLPeerInfo>(actual) {

    public actual class PeerId
    internal constructor(actual: CBLPeerInfo.PeerId) : DelegatedClass<CBLPeerInfo.PeerId>(actual) {

        public actual val bytes: ByteArray
            get() = actual.bytes
    }

    public actual val peerId: PeerId
        get() = PeerId(actual.peerId)

    public actual val certificate: ByteArray?
        get() = actual.certificate?.encoded

    public actual val isOnline: Boolean
        get() = actual.isOnline

    public actual val transports: Set<MultipeerTransport>
        get() = actual.transports

    public actual val replicatorTransport: MultipeerTransport?
        get() = actual.replicatorTransport

    public actual val replicatorStatus: ReplicatorStatus
        get() = ReplicatorStatus(actual.replicatorStatus)

    public actual val neighbors: Set<PeerId>
        get() = actual.neighbors.map { PeerId(it) }.toSet()

    public actual companion object {

        public actual val PEER_ID_LENGTH: Int = CBLPeerInfo.PEER_ID_LENGTH
    }
}
