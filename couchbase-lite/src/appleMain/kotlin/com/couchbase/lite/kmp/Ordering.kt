package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLQueryOrdering
import cocoapods.CouchbaseLite.CBLQuerySortOrder
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.chain

public actual abstract class Ordering
private constructor(actual: CBLQueryOrdering) :
    DelegatedClass<CBLQueryOrdering>(actual) {

    public actual class SortOrder
    internal constructor(override val actual: CBLQuerySortOrder) : Ordering(actual) {

        private inline fun chain(action: CBLQuerySortOrder.() -> Unit) = chain(actual, action)

        public actual fun ascending(): Ordering = chain {
            ascending()
        }

        public actual fun descending(): Ordering = chain {
            descending()
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(CBLQueryOrdering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(CBLQueryOrdering.expression(expression.actual))
    }
}
