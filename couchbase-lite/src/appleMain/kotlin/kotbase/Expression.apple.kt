package kotbase

import cocoapods.CouchbaseLite.CBLQueryExpression
import cocoapods.CouchbaseLite.asJSON
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber
import kotlin.Array

internal actual class ExpressionPlatformState(
    internal val actual: CBLQueryExpression
)

public actual sealed class Expression(actual: CBLQueryExpression) {

    internal actual val platformState: ExpressionPlatformState? = ExpressionPlatformState(actual)

    internal actual open fun asJSON(): Any? = actual.asJSON()

    public actual companion object {

        public actual fun value(value: Any?): Expression =
            ExpressionImpl(CBLQueryExpression.value(value?.actualIfDelegated()))

        public actual fun string(value: String?): Expression =
            ExpressionImpl(CBLQueryExpression.string(value))

        public actual fun number(value: Number?): Expression =
            ExpressionImpl(CBLQueryExpression.number(value as NSNumber?))

        public actual fun intValue(value: Int): Expression =
            ExpressionImpl(CBLQueryExpression.integer(value.convert()))

        public actual fun longValue(value: Long): Expression =
            ExpressionImpl(CBLQueryExpression.longLong(value))

        public actual fun floatValue(value: Float): Expression =
            ExpressionImpl(CBLQueryExpression.float(value))

        public actual fun doubleValue(value: Double): Expression =
            ExpressionImpl(CBLQueryExpression.double(value))

        public actual fun booleanValue(value: Boolean): Expression =
            ExpressionImpl(CBLQueryExpression.boolean(value))

        public actual fun date(value: Instant?): Expression =
            ExpressionImpl(CBLQueryExpression.date(value?.toNSDate()))

        public actual fun map(value: Map<String, Any?>?): Expression =
            ExpressionImpl(CBLQueryExpression.dictionary(value?.actualIfDelegated()))

        public actual fun list(value: List<Any?>?): Expression =
            ExpressionImpl(CBLQueryExpression.array(value?.actualIfDelegated()))

        public actual fun all(): PropertyExpression =
            PropertyExpression("") // CBLPropertyExpression.kCBLAllPropertiesName = ""

        public actual fun property(property: String): PropertyExpression =
            PropertyExpression(property)

        public actual fun parameter(name: String): Expression =
            ExpressionImpl(CBLQueryExpression.parameterNamed(name))

        public actual fun negated(expression: Expression): Expression =
            ExpressionImpl(CBLQueryExpression.negated(expression.actual))

        public actual fun not(expression: Expression): Expression =
            ExpressionImpl(CBLQueryExpression.not(expression.actual))
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
        ExpressionImpl(actual.andExpression(expression.actual))

    public actual fun or(expression: Expression): Expression =
        ExpressionImpl(actual.orExpression(expression.actual))

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
        ExpressionImpl(actual.isValued())

    public actual fun isNotValued(): Expression =
        ExpressionImpl(actual.isNotValued())

    public actual fun collate(collation: Collation): Expression =
        ExpressionImpl(actual.collate(collation.actual))

    public actual fun `in`(vararg expressions: Expression): Expression =
        ExpressionImpl(actual.`in`(expressions.actuals()))

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? Expression)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal class ExpressionImpl(actual: CBLQueryExpression) : Expression(actual)

internal val Expression.actual: CBLQueryExpression
    get() = platformState!!.actual

internal fun Array<out Expression>.actuals(): List<CBLQueryExpression> =
    map { it.actual }
