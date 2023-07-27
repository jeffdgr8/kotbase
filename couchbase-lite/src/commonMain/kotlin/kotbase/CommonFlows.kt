package kotbase

import kotlinx.coroutines.CoroutineDispatcher
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
