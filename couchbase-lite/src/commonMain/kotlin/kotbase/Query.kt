package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * A database query used for querying data from the database. The query statement of the Query
 * object can be fluently constructed by calling the static select methods.
 */
public interface Query {

    /**
     * Returns a copies of the current parameters.
     *
     * Set parameters should copy the given parameters. Set a new parameter will
     * also re-execute the query if there is at least one listener listening for
     * changes.
     */
    public var parameters: Parameters?

    /**
     * Executes the query. The returning a result set that enumerates result rows one at a time.
     * You can run the query any number of times, and you can even have multiple ResultSet active at
     * once.
     *
     * The results come from a snapshot of the database taken at the moment the run() method
     * is called, so they will not reflect any changes made to the database afterwards.
     *
     * @return the ResultSet for the query result.
     * @throws CouchbaseLiteException if there is an error when running the query.
     */
    @Throws(CouchbaseLiteException::class)
    public fun execute(): ResultSet

    /**
     * Returns a string describing the implementation of the compiled query.
     * This is intended to be read by a developer for purposes of optimizing the query, especially
     * to add database indexes. It's not machine-readable and its format may change.
     * As currently implemented, the result is two or more lines separated by newline characters:
     * * The first line is the SQLite SELECT statement.
     * * The subsequent lines are the output of SQLite's "EXPLAIN QUERY PLAN" command applied to that
     * statement; for help interpreting this, see https://www.sqlite.org/eqp.html . The most
     * important thing to know is that if you see "SCAN TABLE", it means that SQLite is doing a
     * slow linear scan of the documents instead of using an index.
     *
     * @return a string describing the implementation of the compiled query.
     * @throws CouchbaseLiteException if an error occurs
     */
    @Throws(CouchbaseLiteException::class)
    public fun explain(): String

    /**
     * Adds a change listener for the changes that occur in the query results.
     *
     * The changes will be delivered on the main thread for platforms that support it: Android, iOS, and macOS.
     * Callbacks are on an arbitrary thread for the JVM, Linux, and Windows platform.
     *
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     */
    public fun addChangeListener(listener: QueryChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur in the query results with a [CoroutineContext]
     * that will be used to launch coroutines the listener will be called on. Coroutines will be launched in
     * a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     */
    public fun addChangeListener(context: CoroutineContext, listener: QueryChangeSuspendListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur in the query results with a [CoroutineScope] that will be used
     * to launch coroutines the listener will be called on. The listener is removed when the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     */
    public fun addChangeListener(scope: CoroutineScope, listener: QueryChangeSuspendListener)

    /**
     * Removes a change listener wih the given listener token.
     *
     * @param token The listener token.
     */
    public fun removeChangeListener(token: ListenerToken)
}
