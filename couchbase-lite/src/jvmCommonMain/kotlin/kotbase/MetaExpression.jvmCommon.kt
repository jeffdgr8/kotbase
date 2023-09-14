package kotbase

import com.couchbase.lite.MetaExpression as CBLMetaExpression

public actual class MetaExpression
internal constructor(actual: CBLMetaExpression) : Expression(actual) {

    public actual fun from(fromAlias: String): Expression =
        DelegatedExpression(actual.from(fromAlias))
}

internal val MetaExpression.actual: CBLMetaExpression
    get() = platformState!!.actual as CBLMetaExpression
