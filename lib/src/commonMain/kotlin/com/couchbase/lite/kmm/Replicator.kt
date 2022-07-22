package com.couchbase.lite.kmm

/**
 * A replicator for replicating document changes between a local database and a target database.
 * The replicator can be bidirectional or either push or pull. The replicator can also be one-shot
 * or continuous. The replicator runs asynchronously, so observe the status to
 * be notified of progress.
 */
public expect class Replicator

/**
 * Initializes a replicator with the given configuration.
 *
 * @param config replicator configuration
 */
constructor(config: ReplicatorConfiguration) {

    internal constructor(config: ReplicatorConfiguration, test: Boolean)

    /**
     * Start the replicator.
     */
    public fun start()

    /**
     * Start the replicator.
     * This method does not wait for the replicator to start.
     * The replicator runs asynchronously and reports its progress
     * through replicator change notifications.
     */
    public fun start(resetCheckpoint: Boolean)

    /**
     * Stop a running replicator.
     * This method does not wait for the replicator to stop.
     * When it does actually stop it will a new state, STOPPED, to change listeners.
     */
    public fun stop()

    /**
     * The replicator's configuration.
     */
    public val config: ReplicatorConfiguration

    /**
     * The replicator's current status: its activity level and progress. Observable.
     */
    public val status: ReplicatorStatus

    /**
     * The server certificates received from the server during the TLS handshake.
     *
     * @return this replicator's server certificates.
     */
    // platform-specific implementations
    //public val serverCertificates: List<Certificate>?

    /**
     * Get a best effort list of documents still pending replication.
     *
     * @return a set of ids for documents still awaiting replication.
     */
    @Throws(CouchbaseLiteException::class)
    public fun getPendingDocumentIds(): Set<String>

    /**
     * Best effort check to see if the document whose ID is passed is still pending replication.
     *
     * @param docId Document id
     * @return true if the document is pending
     */
    @Throws(CouchbaseLiteException::class)
    public fun isDocumentPending(docId: String): Boolean

    /**
     * Adds a change listener for the changes in the replication status and progress.
     *
     * The changes will be delivered on the UI thread for the Android platform
     * On other Java platforms, the callback will occur on an arbitrary thread.
     *
     * When developing a Java Desktop application using Swing or JavaFX that needs to update the UI after
     * receiving the changes, make sure to schedule the UI update on the UI thread by using
     * SwingUtilities.invokeLater(Runnable) or Platform.runLater(Runnable) respectively.
     *
     * @param listener callback
     */
    public fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes in the replication status and progress with an executor on which
     * the changes will be posted to the listener. If the executor is not specified, the changes will be delivered
     * on the UI thread on Android platform and on an arbitrary thread on other Java platform.
     *
     * @param executor executor on which events will be delivered
     * @param listener callback
     */
    // TODO:
    //public fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    /**
     * Adds a listener for receiving the replication status of the specified document. The status will be
     * delivered on the UI thread for the Android platform and on an arbitrary thread for the Java platform.
     * When developing a Java Desktop application using Swing or JavaFX that needs to update the UI after
     * receiving the status, make sure to schedule the UI update on the UI thread by using
     * SwingUtilities.invokeLater(Runnable) or Platform.runLater(Runnable) respectively.
     *
     * @param listener callback
     * @return A ListenerToken that can be used to remove the handler in the future.
     */
    public fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken

    /**
     * Adds a listener for receiving the replication status of the specified document with an executor on which
     * the status will be posted to the listener. If the executor is not specified, the status will be delivered
     * on the UI thread for the Android platform and on an arbitrary thread for the Java platform.
     *
     * @param executor executor on which events will be delivered
     * @param listener callback
     */
    // TODO:
    //public fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    /**
     * Remove the given ReplicatorChangeListener or DocumentReplicationListener from the this replicator.
     *
     * @param token returned by a previous call to addChangeListener or addDocumentListener.
     */
    public fun removeChangeListener(token: ListenerToken)
}
