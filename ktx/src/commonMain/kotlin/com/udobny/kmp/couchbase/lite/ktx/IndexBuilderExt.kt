package com.udobny.kmp.couchbase.lite.ktx

import com.couchbase.lite.kmp.*

/**
 * Create a value index with the given properties to be indexed.
 *
 * @param properties The properties to be indexed
 * @return The value index
 */
public fun valueIndex(vararg properties: String): ValueIndex {
    return IndexBuilder.valueIndex(
        *properties.map { ValueIndexItem.property(it) }.toTypedArray()
    )
}

/**
 * Create a full-text search index with the given properties to be
 * used to perform the match operation against with.
 *
 * @param properties Properties used to perform the match operation against with.
 * @return The full-text search index
 */
public fun fullTextIndex(vararg properties: String): FullTextIndex {
    return IndexBuilder.fullTextIndex(
        *properties.map { FullTextIndexItem.property(it) }.toTypedArray()
    )
}
