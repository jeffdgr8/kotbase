package kotbase

/**
 * The flags enum describing the replicated document.
 */
public expect enum class DocumentFlag {

    /**
     * The current deleted status of the document.
     */
    DELETED,

    /**
     * The current access removed status of the document.
     */
    ACCESS_REMOVED
}