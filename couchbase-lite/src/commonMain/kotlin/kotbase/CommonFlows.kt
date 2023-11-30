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

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.CoroutineContext

/**
 * A Flow of Collection changes.
 *
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 *
 * @see Collection.addChangeListener
 */
public fun Collection.collectionChangeFlow(
    coroutineContext: CoroutineContext? = null
): Flow<CollectionChange> = callbackFlow {
    val token: ListenerToken = addChangeListener(coroutineContext ?: this.coroutineContext) { trySend(it) }
    awaitClose { token.remove() }
}

/**
 * A Flow of document changes
 *
 * @param documentId the document ID
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 *
 * @see Collection.addDocumentChangeListener
 */
public fun Collection.documentChangeFlow(
    documentId: String,
    coroutineContext: CoroutineContext? = null
): Flow<DocumentChange> = callbackFlow {
    val token: ListenerToken = addDocumentChangeListener(documentId, coroutineContext ?: this.coroutineContext) { trySend(it) }
    awaitClose { token.remove() }
}

/**
 * A Flow of replicator state changes.
 *
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 *
 * @see Replicator.addChangeListener
 */
public fun Replicator.replicatorChangesFlow(
    coroutineContext: CoroutineContext? = null
): Flow<ReplicatorChange> = callbackFlow {
    val token: ListenerToken = addChangeListener(coroutineContext ?: this.coroutineContext) { trySend(it) }
    awaitClose { token.remove() }
}

/**
 * A Flow of document replications.
 *
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 *
 * @see Replicator.addDocumentReplicationListener
 */
public fun Replicator.documentReplicationFlow(
    coroutineContext: CoroutineContext? = null
): Flow<DocumentReplication> = callbackFlow {
    val token: ListenerToken = addDocumentReplicationListener(coroutineContext ?: this.coroutineContext) { trySend(it) }
    awaitClose { token.remove() }
}

/**
 * A Flow of query changes.
 *
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 *
 * @see Query.addChangeListener
 */
public fun Query.queryChangeFlow(coroutineContext: CoroutineContext? = null): Flow<QueryChange> = callbackFlow {
    val token: ListenerToken = addChangeListener(coroutineContext ?: this.coroutineContext) { trySend(it) }
    awaitClose { token.remove() }
}

/**
 * A Flow of database changes.
 *
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 *
 * @see Database.addChangeListener
 */
@Suppress("DEPRECATION")
@Deprecated(
    "Use Collection.collectionChangeFlow()",
    ReplaceWith("getDefaultCollection().collectionChangeFlow(coroutineContext)")
)
public fun Database.databaseChangeFlow(
    coroutineContext: CoroutineContext? = null
): Flow<DatabaseChange> = callbackFlow {
    val token: ListenerToken = addChangeListener(coroutineContext ?: this.coroutineContext) { trySend(it) }
    awaitClose { token.remove() }
}

/**
 * A Flow of document changes.
 *
 * @param documentId the document ID
 * @param coroutineContext optional CoroutineContext on which to run the change listener:
 * default is the flow collector's CoroutineContext
 *
 * @see Database.addDocumentChangeListener
 */
@Suppress("DEPRECATION")
@Deprecated(
    "Use Collection.documentChangeFlow()",
    ReplaceWith("getDefaultCollection().documentChangeFlow(documentId, coroutineContext)")
)
public fun Database.documentChangeFlow(
    documentId: String,
    coroutineContext: CoroutineContext? = null
): Flow<DocumentChange> = callbackFlow {
    val token: ListenerToken = addDocumentChangeListener(documentId, coroutineContext ?: this.coroutineContext) { trySend(it) }
    awaitClose { token.remove() }
}
