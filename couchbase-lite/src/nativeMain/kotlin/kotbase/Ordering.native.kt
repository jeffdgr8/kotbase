package kotbase

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual abstract class Ordering
private constructor(internal val expression: Expression) {

    internal abstract fun asJSON(): Any?

    public actual class SortOrder
    internal constructor(
        expression: Expression,
        private var isAscending: Boolean = true
    ) : Ordering(expression) {

        public actual fun ascending(): Ordering {
            isAscending = true
            return this
        }

        public actual fun descending(): Ordering {
            isAscending = false
            return this
        }

        override fun asJSON(): Any? {
            if (isAscending) {
                return expression.asJSON()
            }

            return listOf(
                "DESC",
                expression.asJSON()
            )
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(Expression.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(expression)
    }
}
