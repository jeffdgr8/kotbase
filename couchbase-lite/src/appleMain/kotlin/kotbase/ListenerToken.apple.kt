/*
 * Copyright 2022-2023 Jeff Lockhart
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

import cocoapods.CouchbaseLite.CBLListenerTokenProtocol
import cocoapods.CouchbaseLite.CBLReplicator
import kotbase.internal.DelegatedProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

public actual sealed class ListenerToken(
    actual: CBLListenerTokenProtocol
) : DelegatedProtocol<CBLListenerTokenProtocol>(actual), AutoCloseable {

    actual override fun close() {
        removeImpl()
    }

    public actual fun remove() {
        removeImpl()
    }

    protected open fun removeImpl() {
        actual.remove()
    }
}

internal class DelegatedListenerToken(
    actual: CBLListenerTokenProtocol
) : ListenerToken(actual)

internal class SuspendListenerToken(
    val scope: CoroutineScope,
    actual: CBLListenerTokenProtocol
) : ListenerToken(actual) {

    override fun removeImpl() {
        super.removeImpl()
        scope.cancel()
    }
}

// Workaround for CBLListenerToken.remove not working for Replicator.addDocumentReplicationListener
// https://www.couchbase.com/forums/t/cbl-ios-adddocumentreplicationlistener-listenertoken-remove-fails-to-remove-listener/37695
internal open class ReplicatorListenerToken(
    actual: CBLListenerTokenProtocol,
    private val replicator: CBLReplicator
) : ListenerToken(actual) {

    override fun removeImpl() {
        replicator.removeChangeListenerWithToken(actual)
    }
}

internal class SuspendReplicatorListenerToken(
    val scope: CoroutineScope,
    actual: CBLListenerTokenProtocol,
    replicator: CBLReplicator
) : ReplicatorListenerToken(actual, replicator) {

    override fun removeImpl() {
        super.removeImpl()
        scope.cancel()
    }
}
