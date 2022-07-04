package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLIndexBuilder
import com.udobny.kmm.actuals

public actual object IndexBuilder {

    public actual fun valueIndex(vararg items: ValueIndexItem): ValueIndex =
        ValueIndex(CBLIndexBuilder.valueIndexWithItems(items.actuals()))

    public actual fun fullTextIndex(vararg items: FullTextIndexItem): FullTextIndex =
        FullTextIndex(CBLIndexBuilder.fullTextIndexWithItems(items.actuals()))
}
