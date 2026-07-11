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

import cocoapods.CouchbaseLite.CBLConflict
import cocoapods.CouchbaseLite.CBLDocument
import cocoapods.CouchbaseLite.CBLMultipeerConflictResolverProtocol
import cocoapods.CouchbaseLite.CBLPeerID
import platform.darwin.NSObject

internal fun MultipeerConflictResolver.convert(): CBLMultipeerConflictResolverProtocol {
    return object : NSObject(), CBLMultipeerConflictResolverProtocol {

        override fun resolveConflict(conflict: CBLConflict, forPeer: CBLPeerID): CBLDocument? =
            invoke(PeerInfo.PeerId(forPeer), Conflict(conflict))?.actual
    }
}
