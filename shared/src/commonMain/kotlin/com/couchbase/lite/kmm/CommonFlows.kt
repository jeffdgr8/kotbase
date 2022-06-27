package com.couchbase.lite.kmm

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A Flow of database changes.
 *
 * @see com.couchbase.lite.kmm.Database.addChangeListener
 */
@ExperimentalCoroutinesApi
public fun Database.databaseChangeFlow(): Flow<DatabaseChange> = callbackFlow {
    val token = addChangeListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of document changes.
 *
 * @see com.couchbase.lite.kmm.Database.addDocumentChangeListener
 */
@ExperimentalCoroutinesApi
public fun Database.documentChangeFlow(documentId: String): Flow<DocumentChange> = callbackFlow {
    val token = addDocumentChangeListener(documentId) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of replicator state changes.
 *
 * @see com.couchbase.lite.kmm.Replicator.addChangeListener
 */
@ExperimentalCoroutinesApi
public fun Replicator.replicatorChangesFlow(): Flow<ReplicatorChange> = callbackFlow {
    val token = addChangeListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of document replications.
 *
 * @see com.couchbase.lite.kmm.Replicator.addDocumentReplicationListener
 */
@ExperimentalCoroutinesApi
public fun Replicator.documentReplicationFlow(): Flow<DocumentReplication> = callbackFlow {
    val token = addDocumentReplicationListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}

/**
 * A Flow of query changes.
 *
 * @see com.couchbase.lite.kmm.Query.addChangeListener
 */
@ExperimentalCoroutinesApi
public fun Query.queryChangeFlow(): Flow<QueryChange> = callbackFlow {
    val token = addChangeListener { trySend(it) }
    awaitClose { removeChangeListener(token) }
}
