package kotbase

import com.couchbase.lite.asJSON
import kotbase.ext.toDate
import kotlinx.datetime.Instant
import kotlin.Array
import com.couchbase.lite.Expression as CBLExpression

internal actual class ExpressionPlatformState(
    internal val actual: CBLExpression
)

public actual sealed class Expression(actual: CBLExpression) {

    internal actual val platformState: ExpressionPlatformState? = ExpressionPlatformState(actual)

    internal actual open fun asJSON(): Any? = actual.asJSON()

    public actual companion object {

        public actual fun value(value: Any?): Expression =
            ExpressionImpl(CBLExpression.value(value?.actualIfDelegated()))

        public actual fun string(value: String?): Expression =
            ExpressionImpl(CBLExpression.string(value))

        public actual fun number(value: Number?): Expression =
            ExpressionImpl(CBLExpression.number(value))

        public actual fun intValue(value: Int): Expression =
            ExpressionImpl(CBLExpression.intValue(value))

        public actual fun longValue(value: Long): Expression =
            ExpressionImpl(CBLExpression.longValue(value))

        public actual fun floatValue(value: Float): Expression =
            ExpressionImpl(CBLExpression.floatValue(value))

        public actual fun doubleValue(value: Double): Expression =
            ExpressionImpl(CBLExpression.doubleValue(value))

        public actual fun booleanValue(value: Boolean): Expression =
            ExpressionImpl(CBLExpression.booleanValue(value))

        public actual fun date(value: Instant?): Expression =
            ExpressionImpl(CBLExpression.date(value?.toDate()))

        public actual fun map(value: Map<String, Any?>?): Expression =
            ExpressionImpl(CBLExpression.map(value?.actualIfDelegated()))

        public actual fun list(value: List<Any?>?): Expression =
            ExpressionImpl(CBLExpression.list(value?.actualIfDelegated()))

        public actual fun all(): PropertyExpression =
            PropertyExpression(CBLExpression.all())

        public actual fun property(property: String): PropertyExpression =
            PropertyExpression(CBLExpression.property(property))

        public actual fun parameter(name: String): Expression =
            ExpressionImpl(CBLExpression.parameter(name))

        public actual fun negated(expression: Expression): Expression =
            ExpressionImpl(CBLExpression.negated(expression.actual))

        public actual fun not(expression: Expression): Expression =
            ExpressionImpl(CBLExpression.not(expression.actual))
    }

    public actual fun multiply(expression: Expression): Expression =
        ExpressionImpl(actual.multiply(expression.actual))

    public actual fun divide(expression: Expression): Expression =
        ExpressionImpl(actual.divide(expression.actual))

    public actual fun modulo(expression: Expression): Expression =
        ExpressionImpl(actual.modulo(expression.actual))

    public actual fun add(expression: Expression): Expression =
        ExpressionImpl(actual.add(expression.actual))

    public actual fun subtract(expression: Expression): Expression =
        ExpressionImpl(actual.subtract(expression.actual))

    public actual fun lessThan(expression: Expression): Expression =
        ExpressionImpl(actual.lessThan(expression.actual))

    public actual fun lessThanOrEqualTo(expression: Expression): Expression =
        ExpressionImpl(actual.lessThanOrEqualTo(expression.actual))

    public actual fun greaterThan(expression: Expression): Expression =
        ExpressionImpl(actual.greaterThan(expression.actual))

    public actual fun greaterThanOrEqualTo(expression: Expression): Expression =
        ExpressionImpl(actual.greaterThanOrEqualTo(expression.actual))

    public actual fun equalTo(expression: Expression): Expression =
        ExpressionImpl(actual.equalTo(expression.actual))

    public actual fun notEqualTo(expression: Expression): Expression =
        ExpressionImpl(actual.notEqualTo(expression.actual))

    public actual fun and(expression: Expression): Expression =
        ExpressionImpl(actual.and(expression.actual))

    public actual fun or(expression: Expression): Expression =
        ExpressionImpl(actual.or(expression.actual))

    public actual fun like(expression: Expression): Expression =
        ExpressionImpl(actual.like(expression.actual))

    public actual fun regex(expression: Expression): Expression =
        ExpressionImpl(actual.regex(expression.actual))

    public actual fun `is`(expression: Expression): Expression =
        ExpressionImpl(actual.`is`(expression.actual))

    public actual fun isNot(expression: Expression): Expression =
        ExpressionImpl(actual.isNot(expression.actual))

    public actual fun between(expression1: Expression, expression2: Expression): Expression =
        ExpressionImpl(actual.between(expression1.actual, expression2.actual))

    public actual fun isValued(): Expression =
        ExpressionImpl(actual.isValued)

    public actual fun isNotValued(): Expression =
        ExpressionImpl(actual.isNotValued)

    public actual fun collate(collation: Collation): Expression =
        ExpressionImpl(actual.collate(collation.actual))

    public actual fun `in`(vararg expressions: Expression): Expression =
        ExpressionImpl(actual.`in`(*expressions.actuals()))

    override fun equals(other: Any?): Boolean =
        actual == (other as? Expression)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal class ExpressionImpl(actual: CBLExpression) : Expression(actual)

internal val Expression.actual: CBLExpression
    get() = platformState!!.actual

internal fun Array<out Expression>.actuals(): Array<CBLExpression> =
    map { it.actual }.toTypedArray()
