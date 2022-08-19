package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLIndexBuilder
import com.udobny.kmp.actuals

public actual object IndexBuilder {

    public actual fun valueIndex(vararg items: ValueIndexItem): ValueIndex =
        ValueIndex(CBLIndexBuilder.valueIndexWithItems(items.actuals()))

    public actual fun fullTextIndex(vararg items: FullTextIndexItem): FullTextIndex =
        FullTextIndex(CBLIndexBuilder.fullTextIndexWithItems(items.actuals()))
}
