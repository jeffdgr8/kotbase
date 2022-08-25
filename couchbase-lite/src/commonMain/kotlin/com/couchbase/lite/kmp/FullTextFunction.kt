@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * Full-text function.
 */
public expect object FullTextFunction {

    /**
     * Creates a full-text expression with the given full-text index name and search text.
     *
     * @param indexName The full-text index name.
     * @param text The search text
     * @return The full-text match expression
     */
    public fun match(indexName: String, text: String): Expression

    /**
     * Creates a full-text rank function with the given full-text index name.
     * The rank function indicates how well the current query result matches
     * the full-text query when performing the match comparison.
     *
     * @param indexName The index name.
     * @return The full-text rank function.
     */
    public fun rank(indexName: String): Expression
}
