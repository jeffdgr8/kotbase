package kotbase

/**
 * The listener interface for receiving Live Query change events.
 *
 * The callback function from live query
 *
 * @param change the query change information
 */
public typealias QueryChangeListener = ChangeListener<QueryChange>

/**
 * The listener interface for receiving Live Query change events, called within a coroutine.
 *
 * The callback function from live query
 *
 * @param change the query change information
 */
public typealias QueryChangeSuspendListener = ChangeSuspendListener<QueryChange>
