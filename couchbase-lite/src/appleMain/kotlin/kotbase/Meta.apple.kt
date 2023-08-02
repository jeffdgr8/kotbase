package kotbase

public actual object Meta {

    public actual val id: MetaExpression
        get() = MetaExpression(Expression.property("_id"))

    public actual val revisionID: MetaExpression
        get() = MetaExpression(Expression.property("_revisionID"))

    public actual val sequence: MetaExpression
        get() = MetaExpression(Expression.property("_sequence"))

    public actual val deleted: MetaExpression
        get() = MetaExpression(Expression.property("_deleted"))

    public actual val expiration: MetaExpression
        get() = MetaExpression(Expression.property("_expiration"))
}
