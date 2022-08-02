package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class ValueIndexItem
private constructor(actual: com.couchbase.lite.ValueIndexItem) :
    DelegatedClass<com.couchbase.lite.ValueIndexItem>(actual) {

    public actual companion object {

        public actual fun property(property: String): ValueIndexItem =
            ValueIndexItem(com.couchbase.lite.ValueIndexItem.property(property))

        public actual fun expression(expression: Expression): ValueIndexItem =
            ValueIndexItem(com.couchbase.lite.ValueIndexItem.expression(expression.actual))
    }
}
