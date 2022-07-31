package com.couchbase.lite.kmm

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
