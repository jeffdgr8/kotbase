package com.couchbase.lite.kmp

public actual class FullTextIndexItem
private constructor(internal val expression: Expression) {

    public actual companion object {

        public actual fun property(property: String): FullTextIndexItem =
            FullTextIndexItem(Expression.property(property))
    }
}
