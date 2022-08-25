@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * A meta property expression.
 */
public expect class MetaExpression : Expression {

    /**
     * Specifies an alias name of the data source to query the data from.
     *
     * @param fromAlias The data source alias name.
     * @return The Meta expression with the given alias name specified.
     */
    public fun from(fromAlias: String): Expression
}
