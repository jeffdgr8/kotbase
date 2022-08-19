package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLFullTextIndexItem
import com.udobny.kmp.DelegatedClass

public actual class FullTextIndexItem
private constructor(actual: CBLFullTextIndexItem) :
    DelegatedClass<CBLFullTextIndexItem>(actual) {

    public actual companion object {

        public actual fun property(property: String): FullTextIndexItem =
            FullTextIndexItem(CBLFullTextIndexItem.property(property))
    }
}
