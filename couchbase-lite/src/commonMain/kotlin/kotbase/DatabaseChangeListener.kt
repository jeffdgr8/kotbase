package kotbase

/**
 * The listener interface for receiving Database change events.
 *
 * Callback function from Database when database has changed
 *
 * @param change the database change information
 */
public typealias DatabaseChangeListener = ChangeListener<DatabaseChange>

/**
 * The listener interface for receiving Database change events, called within a coroutine.
 *
 * Callback function from Database when database has changed
 *
 * @param change the database change information
 */
public typealias DatabaseChangeSuspendListener = ChangeSuspendListener<DatabaseChange>
