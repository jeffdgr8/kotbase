package kotbase

/**
 * The listener interface for receiving Replicator change events.
 *
 * The callback function from Replicator
 *
 * @param change the Replicator change information
 */
public typealias ReplicatorChangeListener = ChangeListener<ReplicatorChange>

/**
 * The listener interface for receiving Replicator change events, called within a coroutine.
 *
 * The callback function from Replicator
 *
 * @param change the Replicator change information
 */
public typealias ReplicatorChangeSuspendListener = ChangeSuspendListener<ReplicatorChange>
