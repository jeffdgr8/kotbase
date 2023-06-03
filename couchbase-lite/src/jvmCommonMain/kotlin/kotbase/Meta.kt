package kotbase

public actual object Meta {

    public actual val id: MetaExpression
        get() = MetaExpression(com.couchbase.lite.Meta.id)

    public actual val revisionID: MetaExpression
        get() = MetaExpression(com.couchbase.lite.Meta.revisionID)

    public actual val sequence: MetaExpression
        get() = MetaExpression(com.couchbase.lite.Meta.sequence)

    public actual val deleted: MetaExpression
        get() = MetaExpression(com.couchbase.lite.Meta.deleted)

    public actual val expiration: MetaExpression
        get() = MetaExpression(com.couchbase.lite.Meta.expiration)
}
