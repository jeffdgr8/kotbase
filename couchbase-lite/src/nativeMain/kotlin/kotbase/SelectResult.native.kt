package kotbase

internal actual class SelectResultPlatformState(
    internal val expression: Expression,
    internal var alias: String? = null
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
}

public actual sealed class SelectResult
private constructor(
    expression: Expression,
    alias: String? = null
) {

    internal actual val platformState = SelectResultPlatformState(expression, alias)

    private class SelectResultImpl(expression: Expression) : SelectResult(expression)

    public actual class From
    internal constructor(expression: PropertyExpression) : SelectResult(expression) {

        public actual fun from(alias: String): SelectResult =
            SelectResultImpl(expression.from(alias))
    }

    public actual class As
    internal constructor(expression: Expression) : SelectResult(expression) {

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

internal val SelectResult.expression: Expression
    get() = platformState.expression

internal var SelectResult.alias: String?
    get() = platformState.alias
    set(value) {
        platformState.alias = value
    }

internal fun SelectResult.asJSON(): Any? =
    platformState.asJSON()

internal val SelectResult.From.expression: PropertyExpression
    get() = platformState.expression as PropertyExpression
