package kotbase

/**
 * The listener interface for receiving Document change events.
 *
 * Callback function from Database when the specified document is updated.
 *
 * @param change description of the change
 */
public typealias DocumentChangeListener = ChangeListener<DocumentChange>

/**
 * The listener interface for receiving Document change events, called within a coroutine.
 *
 * Callback function from Database when the specified document is updated.
 *
 * @param change description of the change
 */
public typealias DocumentChangeSuspendListener = ChangeSuspendListener<DocumentChange>
