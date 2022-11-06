package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual abstract class Ordering
private constructor(actual: com.couchbase.lite.Ordering) :
    DelegatedClass<com.couchbase.lite.Ordering>(actual) {

    public actual class SortOrder
    internal constructor(override val actual: com.couchbase.lite.Ordering.SortOrder) :
        Ordering(actual) {

        public actual fun ascending(): Ordering {
            actual.ascending()
            return this
        }

        public actual fun descending(): Ordering {
            actual.descending()
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(com.couchbase.lite.Ordering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(com.couchbase.lite.Ordering.expression(expression.actual))
    }
}
