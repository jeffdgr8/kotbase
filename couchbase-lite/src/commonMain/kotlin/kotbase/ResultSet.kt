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


/**
 * The representation of a query result. The result set is an iterator over
 * [Result] objects.
 */
@OptIn(ExperimentalStdlibApi::class)
public expect class ResultSet : Iterable<Result>, AutoCloseable {

    /**
     * Move the cursor forward one row from its current row position.
     *
     * Caution: [ResultSet.next], [ResultSet.iterator] and [ResultSet.iterator]
     * method share same data structure. They cannot be used together.
     *
     * Caution: When a ResultSet is obtained from a QueryChangeListener and the QueryChangeListener is
     * removed from Query, the ResultSet will be freed and this method will return null.
     *
     * @return the Result after moving the cursor forward. Returns `null` value
     * if there are no more rows, or ResultSet is freed already.
     */
    public operator fun next(): Result?

    /**
     * Return a List of all Results.
     *
     * Caution: [ResultSet.next], [ResultSet.iterator] and [ResultSet.iterator]
     * method share same data structure. They cannot be used together.
     *
     * @return List of Results
     */
    public fun allResults(): List<Result>

    /**
     * Return Iterator of Results.
     *
     * Caution: [ResultSet.next], [ResultSet.iterator] and [ResultSet.iterator]
     * method share same data structure. They cannot be used together.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    override fun iterator(): Iterator<Result>

    override fun close()
}
