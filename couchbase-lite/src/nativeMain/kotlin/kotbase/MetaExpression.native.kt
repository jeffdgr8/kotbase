package kotbase

public actual class MetaExpression
internal constructor(
    private val propertyExpression: PropertyExpression
) : Expression() {

    public actual fun from(fromAlias: String): Expression =
        propertyExpression.from(fromAlias)

    override fun asJSON(): Any =
        propertyExpression.asJSON()
}
