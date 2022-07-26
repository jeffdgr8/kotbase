package com.couchbase.lite.kmm

/**
 * Meta is a factory class for creating the expressions that refer to
 * the metadata properties of the document.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect object Meta {

    /**
     * A metadata expression referring to the ID of the document.
     */
    public val id: MetaExpression

    /**
     * A metadata expression referring to the RevisionId of the document.
     */
    public val revisionID: MetaExpression

    /**
     * A metadata expression referring to the sequence number of the document.
     * The sequence number indicates how recently the document has been changed. If one document's
     * `sequence` is greater than another's, that means it was changed more recently.
     */
    public val sequence: MetaExpression

    /**
     * A metadata expression referring to the deleted boolean flag of the document.
     */
    public val deleted: MetaExpression

    /**
     * A metadata expression referring to the expiration date of the document.
     */
    public val expiration: MetaExpression
}
