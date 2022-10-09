package com.couchbase.lite.kmp

public actual open class SelectResult
private constructor(
    protected val expression: Expression,
    private val alias: String? = null
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
    internal constructor(expression: Expression) : SelectResult(expression) {

        public actual fun from(alias: String): SelectResult =
            SelectResult(expression, alias)
    }

    public actual class As
    internal constructor(
        expression: Expression
    ) : SelectResult(expression) {

        public actual fun `as`(alias: String): SelectResult {
            return SelectResult(expression, alias)
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
