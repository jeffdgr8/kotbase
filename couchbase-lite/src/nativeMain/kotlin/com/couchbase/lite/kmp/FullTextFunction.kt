package com.couchbase.lite.kmp

public actual object FullTextFunction {

    public actual fun match(indexName: String, text: String): Expression =
        Expression.FunctionExpression(
            "MATCH()",
            listOf(Expression.string(indexName), Expression.string(text))
        )

    public actual fun rank(indexName: String): Expression =
        Expression.FunctionExpression("RANK()", listOf(Expression.string(indexName)))
}
