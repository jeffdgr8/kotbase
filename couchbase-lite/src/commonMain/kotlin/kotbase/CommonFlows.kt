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
    val token = addChangeListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of document changes.
 *
 * @see Database.addDocumentChangeListener
 */
public fun Database.documentChangeFlow(documentId: String): Flow<DocumentChange> = callbackFlow {
    val token = addDocumentChangeListener(documentId) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

// TODO: 3.1 APIs
///**
// * A Flow of Collection changes. DatabaseChange should be replaced with CollectionChange once the implementation is ready
// *
// * @see Collection.addChangeListener
// */
//public fun Collection.collectionChangeFlow(): Flow<CollectionChange> = callbackFlow {
//    val token = addChangeListener(CollectionChangeListener { trySend(it) })
//    awaitClose { token.remove() }
//}
//
///**
// * A Flow of document changes
// *
// * @see Collection.addDocumentChangeListener
// */
//public fun Collection.documentChangeFlow(documentId: String): Flow<DocumentChange> = callbackFlow {
//    val token = addDocumentChangeListener(documentId, executor) { trySend(it) }
//    awaitClose { token.remove() }
//}

/**
 * A Flow of replicator state changes.
 *
 * @see Replicator.addChangeListener
 */
public fun Replicator.replicatorChangesFlow(): Flow<ReplicatorChange> = callbackFlow {
    val token = addChangeListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of document replications.
 *
 * @see Replicator.addDocumentReplicationListener
 */
public fun Replicator.documentReplicationFlow(): Flow<DocumentReplication> = callbackFlow {
    val token = addDocumentReplicationListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of query changes.
 *
 * @see Query.addChangeListener
 */
public fun Query.queryChangeFlow(): Flow<QueryChange> = callbackFlow {
    val token = addChangeListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}