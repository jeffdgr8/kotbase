package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.chain

public actual abstract class Ordering
private constructor(actual: com.couchbase.lite.Ordering) :
    DelegatedClass<com.couchbase.lite.Ordering>(actual) {

    public actual class SortOrder
    internal constructor(override val actual: com.couchbase.lite.Ordering.SortOrder) :
        Ordering(actual) {

        private inline fun chain(action: com.couchbase.lite.Ordering.SortOrder.() -> Unit) =
            chain(actual, action)

        public actual fun ascending(): Ordering = chain {
            ascending()
        }

        public actual fun descending(): Ordering = chain {
            descending()
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(com.couchbase.lite.Ordering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(com.couchbase.lite.Ordering.expression(expression.actual))
    }
}
