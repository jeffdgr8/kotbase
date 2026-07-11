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

/**
 * **ENTERPRISE EDITION API**
 *
 * Information about a peer during multipeer replication.
 */
public expect class PeerInfo {

    /**
     * A peer identifier based on 32-byte SHA-256 binary data.
     */
    public class PeerId {

        /**
         * The peer's unique ID: the raw 32-byte SHA-256 data.
         */
        public val bytes: ByteArray
    }

    /**
     * The peer ID.
     */
    public val peerId: PeerId

    /**
     * The peer's TLS certificate. This will only be available after the peer has
     * been authenticated during the connection process established for replication.
     */
    public val certificate: ByteArray?

    /**
     * Whether the peer is online.
     */
    public val isOnline: Boolean

    /**
     * The transports on which the peer was discovered.
     */
    public val transports: Set<MultipeerTransport>

    /**
     * The transport currently used for replication, if any.
     */
    public val replicatorTransport: MultipeerTransport?

    /**
     * The current status of the replicator.
     */
    public val replicatorStatus: ReplicatorStatus

    /**
     * The set of visible neighbors.
     */
    public val neighbors: Set<PeerId>

    public companion object {

        /**
         * The length, in bytes, of a peer ID.
         */
        public val PEER_ID_LENGTH: Int
    }
}
