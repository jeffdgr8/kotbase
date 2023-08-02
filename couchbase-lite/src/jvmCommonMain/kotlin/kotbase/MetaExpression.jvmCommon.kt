package kotbase

import com.couchbase.lite.MetaExpression as CBLMetaExpression

public actual class MetaExpression
internal constructor(override val actual: CBLMetaExpression) : Expression(actual) {

    public actual fun from(fromAlias: String): Expression =
        Expression(actual.from(fromAlias))
}
