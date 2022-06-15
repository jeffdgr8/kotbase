package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLValueIndexItem
import com.udobny.kmm.DelegatedClass

public actual class ValueIndexItem
private constructor(actual: CBLValueIndexItem) :
    DelegatedClass<CBLValueIndexItem>(actual) {

    public actual companion object {

        public actual fun property(property: String): ValueIndexItem =
            ValueIndexItem(CBLValueIndexItem.property(property))

        public actual fun expression(expression: Expression): ValueIndexItem =
            ValueIndexItem(CBLValueIndexItem.expression(expression.actual))
    }
}
