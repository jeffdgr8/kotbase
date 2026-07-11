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
 * The replicator's status for a peer.
 */
public expect class PeerReplicatorStatus {

    /**
     * The associated peer.
     */
    public val peerId: PeerInfo.PeerId

    /**
     * The transport currently used for replication.
     */
    public val transport: MultipeerTransport

    /**
     * A flag indicating the direction of the replication:
     * true if the replicator is outgoing (i.e. initiated the outgoing connection),
     * false otherwise (i.e. accepted the incoming connection).
     */
    public val isOutgoing: Boolean

    /**
     * Replicator status and progress.
     */
    public val status: ReplicatorStatus
}
