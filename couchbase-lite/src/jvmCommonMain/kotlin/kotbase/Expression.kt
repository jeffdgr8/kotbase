package kotbase

import kotbase.base.DelegatedClass
import kotbase.base.actuals
import kotbase.ext.toDate
import kotlinx.datetime.Instant

public actual open class Expression
internal constructor(actual: com.couchbase.lite.Expression) :
    DelegatedClass<com.couchbase.lite.Expression>(actual) {

    public actual companion object {

        public actual fun value(value: Any?): Expression =
            Expression(com.couchbase.lite.Expression.value(value?.actualIfDelegated()))

        public actual fun string(value: String?): Expression =
            Expression(com.couchbase.lite.Expression.string(value))

        public actual fun number(value: Number?): Expression =
            Expression(com.couchbase.lite.Expression.number(value))

        public actual fun intValue(value: Int): Expression =
            Expression(com.couchbase.lite.Expression.intValue(value))

        public actual fun longValue(value: Long): Expression =
            Expression(com.couchbase.lite.Expression.longValue(value))

        public actual fun floatValue(value: Float): Expression =
            Expression(com.couchbase.lite.Expression.floatValue(value))

        public actual fun doubleValue(value: Double): Expression =
            Expression(com.couchbase.lite.Expression.doubleValue(value))

        public actual fun booleanValue(value: Boolean): Expression =
            Expression(com.couchbase.lite.Expression.booleanValue(value))

        public actual fun date(value: Instant?): Expression =
            Expression(com.couchbase.lite.Expression.date(value?.toDate()))

        public actual fun map(value: Map<String, Any?>?): Expression =
            Expression(com.couchbase.lite.Expression.map(value?.actualIfDelegated()))

        public actual fun list(value: List<Any?>?): Expression =
            Expression(com.couchbase.lite.Expression.list(value?.actualIfDelegated()))

        public actual fun all(): PropertyExpression =
            PropertyExpression(com.couchbase.lite.Expression.all())

        public actual fun property(property: String): PropertyExpression =
            PropertyExpression(com.couchbase.lite.Expression.property(property))

        public actual fun parameter(name: String): Expression =
            Expression(com.couchbase.lite.Expression.parameter(name))

        public actual fun negated(expression: Expression): Expression =
            Expression(com.couchbase.lite.Expression.negated(expression.actual))

        public actual fun not(expression: Expression): Expression =
            Expression(com.couchbase.lite.Expression.not(expression.actual))
    }

    public actual fun multiply(expression: Expression): Expression =
        Expression(actual.multiply(expression.actual))

    public actual fun divide(expression: Expression): Expression =
        Expression(actual.divide(expression.actual))

    public actual fun modulo(expression: Expression): Expression =
        Expression(actual.modulo(expression.actual))

    public actual fun add(expression: Expression): Expression =
        Expression(actual.add(expression.actual))

    public actual fun subtract(expression: Expression): Expression =
        Expression(actual.subtract(expression.actual))

    public actual fun lessThan(expression: Expression): Expression =
        Expression(actual.lessThan(expression.actual))

    public actual fun lessThanOrEqualTo(expression: Expression): Expression =
        Expression(actual.lessThanOrEqualTo(expression.actual))

    public actual fun greaterThan(expression: Expression): Expression =
        Expression(actual.greaterThan(expression.actual))

    public actual fun greaterThanOrEqualTo(expression: Expression): Expression =
        Expression(actual.greaterThanOrEqualTo(expression.actual))

    public actual fun equalTo(expression: Expression): Expression =
        Expression(actual.equalTo(expression.actual))

    public actual fun notEqualTo(expression: Expression): Expression =
        Expression(actual.notEqualTo(expression.actual))

    public actual fun and(expression: Expression): Expression =
        Expression(actual.and(expression.actual))

    public actual fun or(expression: Expression): Expression =
        Expression(actual.or(expression.actual))

    public actual fun like(expression: Expression): Expression =
        Expression(actual.like(expression.actual))

    public actual fun regex(expression: Expression): Expression =
        Expression(actual.regex(expression.actual))

    public actual fun `is`(expression: Expression): Expression =
        Expression(actual.`is`(expression.actual))

    public actual fun isNot(expression: Expression): Expression =
        Expression(actual.isNot(expression.actual))

    public actual fun between(expression1: Expression, expression2: Expression): Expression =
        Expression(actual.between(expression1.actual, expression2.actual))

    public actual fun isValued(): Expression =
        Expression(actual.isValued)

    public actual fun isNotValued(): Expression =
        Expression(actual.isNotValued)

    public actual fun collate(collation: Collation): Expression =
        Expression(actual.collate(collation.actual))

    public actual fun `in`(vararg expressions: Expression): Expression =
        Expression(actual.`in`(*expressions.actuals()))
}
