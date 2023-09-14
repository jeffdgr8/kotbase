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
            DelegatedExpression(CBLExpression.value(value?.actualIfDelegated()))

        public actual fun string(value: String?): Expression =
            DelegatedExpression(CBLExpression.string(value))

        public actual fun number(value: Number?): Expression =
            DelegatedExpression(CBLExpression.number(value))

        public actual fun intValue(value: Int): Expression =
            DelegatedExpression(CBLExpression.intValue(value))

        public actual fun longValue(value: Long): Expression =
            DelegatedExpression(CBLExpression.longValue(value))

        public actual fun floatValue(value: Float): Expression =
            DelegatedExpression(CBLExpression.floatValue(value))

        public actual fun doubleValue(value: Double): Expression =
            DelegatedExpression(CBLExpression.doubleValue(value))

        public actual fun booleanValue(value: Boolean): Expression =
            DelegatedExpression(CBLExpression.booleanValue(value))

        public actual fun date(value: Instant?): Expression =
            DelegatedExpression(CBLExpression.date(value?.toDate()))

        public actual fun map(value: Map<String, Any?>?): Expression =
            DelegatedExpression(CBLExpression.map(value?.actualIfDelegated()))

        public actual fun list(value: List<Any?>?): Expression =
            DelegatedExpression(CBLExpression.list(value?.actualIfDelegated()))

        public actual fun all(): PropertyExpression =
            PropertyExpression(CBLExpression.all())

        public actual fun property(property: String): PropertyExpression =
            PropertyExpression(CBLExpression.property(property))

        public actual fun parameter(name: String): Expression =
            DelegatedExpression(CBLExpression.parameter(name))

        public actual fun negated(expression: Expression): Expression =
            DelegatedExpression(CBLExpression.negated(expression.actual))

        public actual fun not(expression: Expression): Expression =
            DelegatedExpression(CBLExpression.not(expression.actual))
    }

    public actual fun multiply(expression: Expression): Expression =
        DelegatedExpression(actual.multiply(expression.actual))

    public actual fun divide(expression: Expression): Expression =
        DelegatedExpression(actual.divide(expression.actual))

    public actual fun modulo(expression: Expression): Expression =
        DelegatedExpression(actual.modulo(expression.actual))

    public actual fun add(expression: Expression): Expression =
        DelegatedExpression(actual.add(expression.actual))

    public actual fun subtract(expression: Expression): Expression =
        DelegatedExpression(actual.subtract(expression.actual))

    public actual fun lessThan(expression: Expression): Expression =
        DelegatedExpression(actual.lessThan(expression.actual))

    public actual fun lessThanOrEqualTo(expression: Expression): Expression =
        DelegatedExpression(actual.lessThanOrEqualTo(expression.actual))

    public actual fun greaterThan(expression: Expression): Expression =
        DelegatedExpression(actual.greaterThan(expression.actual))

    public actual fun greaterThanOrEqualTo(expression: Expression): Expression =
        DelegatedExpression(actual.greaterThanOrEqualTo(expression.actual))

    public actual fun equalTo(expression: Expression): Expression =
        DelegatedExpression(actual.equalTo(expression.actual))

    public actual fun notEqualTo(expression: Expression): Expression =
        DelegatedExpression(actual.notEqualTo(expression.actual))

    public actual fun and(expression: Expression): Expression =
        DelegatedExpression(actual.and(expression.actual))

    public actual fun or(expression: Expression): Expression =
        DelegatedExpression(actual.or(expression.actual))

    public actual fun like(expression: Expression): Expression =
        DelegatedExpression(actual.like(expression.actual))

    public actual fun regex(expression: Expression): Expression =
        DelegatedExpression(actual.regex(expression.actual))

    public actual fun `is`(expression: Expression): Expression =
        DelegatedExpression(actual.`is`(expression.actual))

    public actual fun isNot(expression: Expression): Expression =
        DelegatedExpression(actual.isNot(expression.actual))

    public actual fun between(expression1: Expression, expression2: Expression): Expression =
        DelegatedExpression(actual.between(expression1.actual, expression2.actual))

    public actual fun isValued(): Expression =
        DelegatedExpression(actual.isValued)

    public actual fun isNotValued(): Expression =
        DelegatedExpression(actual.isNotValued)

    public actual fun collate(collation: Collation): Expression =
        DelegatedExpression(actual.collate(collation.actual))

    public actual fun `in`(vararg expressions: Expression): Expression =
        DelegatedExpression(actual.`in`(*expressions.actuals()))

    override fun equals(other: Any?): Boolean =
        actual == (other as? Expression)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal class DelegatedExpression(actual: CBLExpression) : Expression(actual)

internal val Expression.actual: CBLExpression
    get() = platformState!!.actual

internal fun Array<out Expression>.actuals(): Array<CBLExpression> =
    map { it.actual }.toTypedArray()
