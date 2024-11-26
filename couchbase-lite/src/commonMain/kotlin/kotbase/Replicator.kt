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

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * A replicator for replicating document changes between a local database and a target database.
 * The replicator can be bidirectional or either push or pull. The replicator can also be one-shot
 * or continuous. The replicator runs asynchronously, so observe the status to
 * be notified of progress.
 */
@OptIn(ExperimentalStdlibApi::class)
public expect class Replicator

/**
 * Initializes a replicator with the given configuration.
 *
 * @param config replicator configuration
 */
constructor(config: ReplicatorConfiguration) : AutoCloseable {

    internal constructor(config: ReplicatorConfiguration, test: Boolean)

    /**
     * Start the replicator.
     */
    public fun start()

    /**
     * Start the replicator.
     *
     * This method does not wait for the replicator to start.
     * The replicator runs asynchronously and reports its progress
     * through replicator change notifications.
     *
     * Note: Replicators **cannot** be started from within a `Database.inBatch()` block.
     */
    public fun start(resetCheckpoint: Boolean)

    /**
     * Stop a running replicator.
     * This method does not wait for the replicator to stop.
     * When the replicator actually stops, it will broadcast a new state, STOPPED,
     * to change listeners.
     */
    public fun stop()

    /**
     * The replicator's configuration.
     */
    public val config: ReplicatorConfiguration

    /**
     * The replicator's current status: its activity level and progress.
     */
    public val status: ReplicatorStatus

    /**
     * The server certificates received from the server during the TLS handshake.
     *
     * @return this replicator's server certificates.
     */
    public val serverCertificates: List<ByteArray>?

    /**
     * Get a best effort set of document IDs in the default collection, that are still pending replication.
     */
    @Deprecated(
        "Use getPendingDocumentIds(Collection)",
        ReplaceWith("getPendingDocumentIds(config.database.defaultCollection)")
    )
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val pendingDocumentIds: Set<String>

    /**
     * Get a best effort list of documents in the passed collection that are still pending replication.
     *
     * @return a set of ids for documents in the passed collection still awaiting replication.
     */
    @Throws(CouchbaseLiteException::class)
    public fun getPendingDocumentIds(collection: Collection): Set<String>

    /**
     * Best effort check to see if the document whose ID is passed is still pending replication.
     *
     * @param docId Document id
     * @return true if the document is pending
     */
    @Deprecated(
        "Use isDocumentPending(String, Collection)",
        ReplaceWith("isDocumentPending(docId, config.database.defaultCollection)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun isDocumentPending(docId: String): Boolean

    /**
     * Best effort check to see if the document whose ID is passed is still pending replication.
     *
     * @param docId Document id
     * @return true if the document is pending
     */
    @Throws(CouchbaseLiteException::class)
    public fun isDocumentPending(docId: String, collection: Collection): Boolean

    /**
     * Adds a change listener for the changes in the replication status and progress.
     *
     * The changes will be delivered on the main thread for platforms that support it: Android, iOS, and macOS.
     * Callbacks are on an arbitrary thread for the JVM, Linux, and Windows platform.
     *
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes in the replication status and progress with a [CoroutineContext]
     * that will be used to launch coroutines the listener will be called on. Coroutines will be launched in
     * a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addChangeListener(context: CoroutineContext, listener: ReplicatorChangeSuspendListener): ListenerToken

    /**
     * Adds a change listener for the changes in the replication status and progress with a [CoroutineScope]
     * that will be used to launch coroutines the listener will be called on. The listener is removed when
     * the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to post changes.
     */
    public fun addChangeListener(scope: CoroutineScope, listener: ReplicatorChangeSuspendListener)

    /**
     * Adds a document replication event listener.
     *
     * The changes will be delivered on the main thread for platforms that support it: Android, iOS, and macOS.
     * Callbacks are on an arbitrary thread for the JVM, Linux, and Windows platform.
     *
     * According to performance optimization in the replicator, the document replication listeners need to be added
     * before starting the replicator. If the listeners are added after the replicator is started, the replicator needs
     * to be stopped and restarted again to ensure that the listeners will get the document replication events.
     *
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken

    /**
     * Adds a document replication event listener with a [CoroutineContext] that will be used to launch coroutines
     * the listener will be called on. Coroutines will be launched in a [CoroutineScope] that is canceled when the
     * listener is removed.
     *
     * According to performance optimization in the replicator, the document replication listeners need to be added
     * before starting the replicator. If the listeners are added after the replicator is started, the replicator needs
     * to be stopped and restarted again to ensure that the listeners will get the document replication events.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addDocumentReplicationListener(
        context: CoroutineContext,
        listener: DocumentReplicationSuspendListener
    ): ListenerToken

    /**
     * Adds a document replication event listener with a [CoroutineScope] that will be used to launch coroutines
     * the listener will be called on. The listener is removed when the scope is canceled.
     *
     * According to performance optimization in the replicator, the document replication listeners need to be added
     * before starting the replicator. If the listeners are added after the replicator is started, the replicator needs
     * to be stopped and restarted again to ensure that the listeners will get the document replication events.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to post changes.
     */
    public fun addDocumentReplicationListener(scope: CoroutineScope, listener: DocumentReplicationSuspendListener)

    /**
     * Remove the given ReplicatorChangeListener or DocumentReplicationListener from the replicator.
     *
     * @param token returned by a previous call to [addChangeListener] or [addDocumentReplicationListener].
     */
    @Deprecated(
        "Use ListenerToken.remove",
        ReplaceWith("token.remove()")
    )
    public fun removeChangeListener(token: ListenerToken)

    /**
     * Immediately close the replicator and free its resources.
     * We recommend the use of this method on Replicators that are in the STOPPED state.  If the
     * replicator is not stopped, this method will make a best effort attempt to stop it but
     * will not wait to confirm that it was stopped cleanly.
     * Any attempt to restart a closed replicator will result in a CouchbaseLiteError.
     * This includes calls to getPendingDocIds and isDocPending.
     */
    override fun close()

    /**
     * Determine whether this replicator has been closed.
     */
    public val isClosed: Boolean
}
