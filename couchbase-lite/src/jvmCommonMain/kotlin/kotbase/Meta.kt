package kotbase

import com.couchbase.lite.Meta as CBLMeta

public actual object Meta {

    public actual val id: MetaExpression
        get() = MetaExpression(CBLMeta.id)

    public actual val revisionID: MetaExpression
        get() = MetaExpression(CBLMeta.revisionID)

    public actual val sequence: MetaExpression
        get() = MetaExpression(CBLMeta.sequence)

    public actual val deleted: MetaExpression
        get() = MetaExpression(CBLMeta.deleted)

    public actual val expiration: MetaExpression
        get() = MetaExpression(CBLMeta.expiration)
}
