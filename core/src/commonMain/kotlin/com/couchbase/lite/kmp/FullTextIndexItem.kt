package com.couchbase.lite.kmp

/**
 * Full-text Index Item.
 */
public expect class FullTextIndexItem {

    public companion object {

        /**
         * Creates a full-text search index item with the given property.
         *
         * @param property A property used to perform the match operation against with.
         * @return The full-text search index item.
         */
        public fun property(property: String): FullTextIndexItem
    }
}
