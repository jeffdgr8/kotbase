package kotbase

public actual class MetaExpression
internal constructor(private val propertyExpression: PropertyExpression) :
    Expression(propertyExpression.actual) {

    public actual fun from(fromAlias: String): Expression =
        propertyExpression.from(fromAlias)
}
