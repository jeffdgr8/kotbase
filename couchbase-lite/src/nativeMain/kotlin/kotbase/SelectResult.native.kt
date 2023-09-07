package kotbase

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual open class SelectResult
private constructor(
    protected open val expression: Expression,
    protected var alias: String? = null
) {

    internal fun asJSON(): Any? {
        if (alias == null) {
            return expression.asJSON()
        }
        return listOf(
            "AS",
            expression.asJSON(),
            alias
        )
    }

    public actual class From
    internal constructor(override val expression: PropertyExpression) : SelectResult(expression) {

        public actual fun from(alias: String): SelectResult =
            SelectResult(expression.from(alias))
    }

    public actual class As
    internal constructor(
        expression: Expression
    ) : SelectResult(expression) {

        public actual fun `as`(alias: String): As {
            this.alias = alias
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): As =
            As(Expression.property(property))

        public actual fun expression(expression: Expression): As =
            As(expression)

        public actual fun all(): From =
            From(Expression.all())
    }
}
