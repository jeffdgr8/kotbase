package kotbase

/**
 * A result set representing the query result. The result set is an iterator of
 * the [Result] objects.
 */
public expect class ResultSet : Iterable<Result>, AutoCloseable {

    /**
     * Move the cursor forward one row from its current row position.
     * Caution: next() method and iterator() method share same data structure.
     * Please don't use them together.
     * Caution: In case ResultSet is obtained from QueryChangeListener, and QueryChangeListener is
     * already removed from Query, ResultSet is already freed. And this next() method returns null.
     *
     * @return the Result after moving the cursor forward. Returns `null` value
     * if there are no more rows, or ResultSet is freed already.
     */
    public operator fun next(): Result?

    /**
     * Return List of Results. List is unmodifiable and only supports
     * int get(int index), int size(), boolean isEmpty() and Iterator&lt;Result&gt; iterator() methods.
     * Once called allResults(), next() method return null. Don't call next() and allResults()
     * together.
     *
     * @return List of Results
     */
    public fun allResults(): List<Result>

    /**
     * Return Iterator of Results.
     * Once called iterator(), next() method return null. Don't call next() and iterator()
     * together.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    override fun iterator(): Iterator<Result>
}
