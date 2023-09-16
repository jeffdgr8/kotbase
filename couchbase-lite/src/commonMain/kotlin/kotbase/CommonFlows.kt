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

/**
 * A Flow of database changes.
 *
 * @see Database.addChangeListener
 */
public fun Database.databaseChangeFlow(): Flow<DatabaseChange> = callbackFlow {
    val token = addChangeListener(coroutineContext) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of document changes.
 *
 * @see Database.addDocumentChangeListener
 */
public fun Database.documentChangeFlow(documentId: String): Flow<DocumentChange> = callbackFlow {
    val token = addDocumentChangeListener(documentId, coroutineContext) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of replicator state changes.
 *
 * @see Replicator.addChangeListener
 */
public fun Replicator.replicatorChangesFlow(): Flow<ReplicatorChange> = callbackFlow {
    val token = addChangeListener(coroutineContext) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of document replications.
 *
 * @see Replicator.addDocumentReplicationListener
 */
public fun Replicator.documentReplicationFlow(): Flow<DocumentReplication> = callbackFlow {
    val token = addDocumentReplicationListener(coroutineContext) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of query changes.
 *
 * @see Query.addChangeListener
 */
public fun Query.queryChangeFlow(): Flow<QueryChange> = callbackFlow {
    val token = addChangeListener(coroutineContext) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}
