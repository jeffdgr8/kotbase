package kotbase

internal actual class OrderingPlatformState(
    private val expression: Expression,
    internal var isAscending: Boolean = true
) {

    internal fun asJSON(): Any? {
        if (isAscending) {
            return expression.asJSON()
        }

        return listOf(
            "DESC",
            expression.asJSON()
        )
    }
}

public actual sealed class Ordering
private constructor(expression: Expression) {

    internal actual val platformState = OrderingPlatformState(expression)

    public actual class SortOrder
    internal constructor(
        expression: Expression
    ) : Ordering(expression) {

        public actual fun ascending(): Ordering {
            isAscending = true
            return this
        }

        public actual fun descending(): Ordering {
            isAscending = false
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(Expression.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(expression)
    }
}

internal var Ordering.isAscending: Boolean
    get() = platformState.isAscending
    set(value) {
        platformState.isAscending = value
    }

internal fun Ordering.asJSON(): Any? =
    platformState.asJSON()
