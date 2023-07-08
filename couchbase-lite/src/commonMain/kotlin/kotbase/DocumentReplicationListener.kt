package kotbase

/**
 * The listener interface for receiving Document replicated events.
 *
 * The callback function from Replicator
 *
 * @param replication the Document replicated information
 */
public typealias DocumentReplicationListener = (replication: DocumentReplication) -> Unit

/**
 * The listener interface for receiving Document replicated events, called within a coroutine.
 *
 * The callback function from Replicator
 *
 * @param replication the Document replicated information
 */
public typealias DocumentReplicationSuspendListener = suspend (replication: DocumentReplication) -> Unit
