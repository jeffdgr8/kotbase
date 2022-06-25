package com.couchbase.lite.kmm

import com.udobny.kmm.actuals

public actual object IndexBuilder {

    public actual fun valueIndex(vararg items: ValueIndexItem): ValueIndex =
        ValueIndex(com.couchbase.lite.IndexBuilder.valueIndex(*items.actuals()))

    public actual fun fullTextIndex(vararg items: FullTextIndexItem): FullTextIndex =
        FullTextIndex(com.couchbase.lite.IndexBuilder.fullTextIndex(*items.actuals()))
}
