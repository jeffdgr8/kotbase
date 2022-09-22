package com.couchbase.lite.kmp

public actual object IndexBuilder {

    public actual fun valueIndex(vararg items: ValueIndexItem): ValueIndex =
        ValueIndex(items.toList())

    public actual fun fullTextIndex(vararg items: FullTextIndexItem): FullTextIndex =
        FullTextIndex(items.toList())
}
