package kotbase.ktx

import kotbase.*

/**
 * Create a value index with the given properties to be indexed.
 *
 * @param properties The properties to be indexed
 * @return The value index
 */
public fun valueIndex(vararg properties: String): ValueIndex =
    IndexBuilder.valueIndex(
        *properties.map { ValueIndexItem.property(it) }.toTypedArray()
    )

/**
 * Create a full-text search index with the given properties to be
 * used to perform the match operation against with.
 *
 * @param properties Properties used to perform the match operation against with.
 * @return The full-text search index
 */
public fun fullTextIndex(vararg properties: String): FullTextIndex =
    IndexBuilder.fullTextIndex(
        *properties.map { FullTextIndexItem.property(it) }.toTypedArray()
    )
