package kotbase

public actual class PropertyExpression
internal constructor(override val actual: com.couchbase.lite.PropertyExpression) :
    Expression(actual) {

    public actual fun from(fromAlias: String): Expression =
        Expression(actual.from(fromAlias))
}
