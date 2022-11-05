package com.couchbase.lite.kmp

/**
 * DocumentFragment provides access to a document object. DocumentFragment
 * also provides subscript access by either key or index to the data
 * values of the document which are wrapped by Fragment objects.
 */
public class DocumentFragment
internal constructor(
    /**
     * Gets the document from the document fragment object.
     */
    public val document: Document? = null
) {

    /**
     * Checks whether the document exists in the database or not.
     */
    public val exists: Boolean
        get() = document != null

    /**
     * Subscript access to a Fragment object by key.
     *
     * @param key The key.
     */
    public operator fun get(key: String): Fragment {
        return if (document != null) {
            document[key]
        } else {
            Fragment()
        }
    }
}
