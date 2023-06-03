package kotbase

public actual class ValueIndexItem
private constructor(internal val expression: Expression) {

    public actual companion object {

        public actual fun property(property: String): ValueIndexItem =
            ValueIndexItem(Expression.property(property))

        public actual fun expression(expression: Expression): ValueIndexItem =
            ValueIndexItem(expression)
    }
}
