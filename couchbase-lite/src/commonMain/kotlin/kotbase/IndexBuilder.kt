package kotbase

/**
 * IndexBuilder used for building database index objects.
 * Use Database.createIndex(IndexConfiguration, String)
 */
public expect object IndexBuilder {

    /**
     * Create a value index with the given index items. The index items are a list of
     * the properties or expressions to be indexed.
     *
     * @param items The index items
     * @return The value index
     */
    public fun valueIndex(vararg items: ValueIndexItem): ValueIndex

    /**
     * Create a full-text search index with the given index item and options. Typically the index item is
     * the property that is used to perform the match operation against with.
     *
     * @param items The index items.
     * @return The full-text search index.
     */
    public fun fullTextIndex(vararg items: FullTextIndexItem): FullTextIndex
}
