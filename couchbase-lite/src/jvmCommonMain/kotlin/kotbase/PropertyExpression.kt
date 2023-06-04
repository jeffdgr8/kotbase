package kotbase

import com.couchbase.lite.PropertyExpression as CBLPropertyExpression

public actual class PropertyExpression
internal constructor(override val actual: CBLPropertyExpression) : Expression(actual) {

    public actual fun from(fromAlias: String): Expression =
        Expression(actual.from(fromAlias))
}
