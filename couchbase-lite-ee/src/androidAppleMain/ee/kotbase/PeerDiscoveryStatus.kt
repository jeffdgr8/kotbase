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
 * Discovery status of a specific peer.
 */
public expect class PeerDiscoveryStatus {

    /**
     * The associated peer.
     */
    public val peer: PeerInfo.PeerId

    /**
     * Whether the peer is discovered (online) or no longer visible (offline): best effort.
     */
    public val isOnline: Boolean

    /**
     * The transport on which the peer was discovered.
     */
    public val transport: MultipeerTransport
}
