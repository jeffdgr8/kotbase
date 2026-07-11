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
import com.couchbase.lite.PeerReplicatorStatus as CBLPeerReplicatorStatus

public actual class PeerReplicatorStatus
internal constructor(actual: CBLPeerReplicatorStatus) : DelegatedClass<CBLPeerReplicatorStatus>(actual) {

    public actual val peerId: PeerInfo.PeerId
        get() = PeerInfo.PeerId(actual.peerId)

    public actual val transport: MultipeerTransport
        get() = actual.transport

    public actual val isOutgoing: Boolean
        get() = actual.isOutgoing

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(actual.status)
}
